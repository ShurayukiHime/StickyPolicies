package com.example.giada.stickypoliciesapp;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giada.stickypoliciesapp.crypto.CryptoUtilities;
import com.example.giada.stickypoliciesapp.utilities.NetworkUtils;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.json.JSONObject;
import org.json.XML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.Key;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class PolicyClient extends AppCompatActivity {

    private TextView mUrlDisplayTextView;
    private Button mObtainCertificateButton;
    private TextView mSearchResultsTextView;
    private Button mSendPiiButton;
    private Button mAccessPolicyButton;

    private String TAG = "PolicyClient";

    final static String OBTAIN_CERT_PATH = "certificates";
    final static String DATA_ACCESS_PATH = "access";
    final static String textPlainContentType = "text/plain; charset=utf-8";
    final static String applicationJsonContentType = "application/json; charset=utf-8";
    private String filename = "encodedDataAndPolicy";


    private X509Certificate taCertificate;
    private String pii = "this is some very personal data!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_client);
        Log.d(TAG, "entered policy client!");

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_search_results_json);

        mObtainCertificateButton = (Button) findViewById(R.id.obtain_certificate_button);
        mObtainCertificateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "obtain certificate button Clicked!");
                certificateRequest();
            }
        });

        mSendPiiButton = (Button) findViewById(R.id.send_pii_button);
        mSendPiiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "send pii button Clicked!");
                shareEncryptedData();
            }
        });

        mAccessPolicyButton = (Button) findViewById(R.id.access_policy_button);
        mAccessPolicyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "access encrypted data button Clicked!");
                accessEncryptedData();
            }
        });
    }

    private void accessEncryptedData() {
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH, "","");
        mUrlDisplayTextView.setText(serverURL.toString());

        // 1) separate encrypted PII from the rest
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = this.getApplicationContext().openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, "File not found!");
        }
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            int read = inputStreamReader.read();
            while (read != -1) {
                byteArrayOutputStream.write(read);
                read = inputStreamReader.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error when reading the encrypted data from the stream");
        }

        // 2) contact TA and send stuff
        // 3) obtain key
        // 4) decryptSymmetric & profit

        new GetTrustedAuthorityCertificateTask().execute(serverURL);
    }

    private void shareEncryptedData() {
        // 1) retrieve policy
        String fileName = "policy1";
        String policy = null;
        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier(fileName, "raw", getPackageName()));
        Scanner scanner = new Scanner(ins);
        scanner.useDelimiter("\\A");
        boolean hasInput = scanner.hasNext();
        if (hasInput)
            policy = scanner.next();
        else
            Log.d(TAG, "Empty file");
        try {
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error closing input stream: " + e.getMessage());
        }

        // 2) generate symmetric disposable encryption key
        byte[] encodedSymmetricKey = CryptoUtilities.generateSymmetricRandomKey();
        // 3) encrypt PII
        byte[] encryptedPii = CryptoUtilities.encryptSymmetric(encodedSymmetricKey, pii.getBytes(Charset.forName("UTF-8")));
        // 4) hash policy
        byte[] policyDigest = new byte[0];
        try {
            policyDigest = CryptoUtilities.calculateDigest(policy.getBytes(Charset.forName("UTF-8")));
        } catch (DigestException e) {
            e.printStackTrace();
            Log.d(TAG, "Error calculating policy digest: " + e.getMessage());
        }
        // 5) concatenate policy and  digest, encrypt with PubTa
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
        try {
            byteArrayOutputStream.write(encodedSymmetricKey);
            byteArrayOutputStream.write(policyDigest);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error in concatenating policy and digest: " + e.getMessage());
        }
        Key publicKey = null;
        if(taCertificate == null) {
            Toast.makeText(this.getApplicationContext(), "Please wait... Cryptography is working for you!", Toast.LENGTH_LONG);
            URL serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "action", "obtainTAcertificate");
            mUrlDisplayTextView.setText(serverURL.toString());
            X509Certificate certificate = null;
            try {
                certificate = new GetTrustedAuthorityCertificateTask().execute(serverURL).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in retrieving certificate: " + e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in retrieving certificate: " + e.getMessage());
            }
            publicKey = certificate.getPublicKey();
        } else
            publicKey = taCertificate.getPublicKey();
        byte[] keyAndHashEncrypted = CryptoUtilities.encryptAsymmetric(publicKey, byteArrayOutputStream.toByteArray());

        //verify if keys have been generated:
        CryptoUtilities.getKeyPair();
        // 6) sign
        byte[] signedEncrPolicyAndHash = new byte[0];
        try {
            signedEncrPolicyAndHash = CryptoUtilities.sign(keyAndHashEncrypted);
        } catch (SignatureException e) {
            e.printStackTrace();
            Log.d(TAG, "Error when signing data: " + e.getMessage());
        }
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error when closing byte array output stream: " + e.getMessage());
        }

        // save or share somewhere somehow
        JSONObject postData = new JSONObject();
        try {
            postData.put("policy", XML.toJSONObject(policy).toString());
            postData.put("taEncryption", Base64.encodeToString(keyAndHashEncrypted, Base64.URL_SAFE));
            postData.put("ownerSignature", Base64.encodeToString(signedEncrPolicyAndHash, Base64.URL_SAFE));
            postData.put("encryptedPii", Base64.encodeToString(encryptedPii, Base64.URL_SAFE));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        new SendEncryptedDataTask(postData.toString()).execute(serverURL);
    }

    private void certificateRequest() {
        URL serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "action", "obtainTAcertificate");
        mUrlDisplayTextView.setText(serverURL.toString());
        new GetTrustedAuthorityCertificateTask().execute(serverURL);

        serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        new SendMyCertificateTask().execute(serverURL);
    }

    public class SendEncryptedDataTask extends  AsyncTask<URL, Void, Void> {
        String postData;

        SendEncryptedDataTask(String postData) {
            this.postData = postData;
        }
        @Override
        protected Void doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            try {
                NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", postData, applicationJsonContentType);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error when sending encrypted POST: " + e.getMessage());
            }
            return null;
        }
    }

    public class SendMyCertificateTask extends  AsyncTask<URL, Void, Void> {
        @Override
        protected void onPreExecute() {
            // generate certificate here hoping to lighten computation afterwards
            try {
                CryptoUtilities.getCertificate();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Errore nella generazione del certificato");
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            StringWriter sw = new StringWriter();
            JcaPEMWriter pw = new JcaPEMWriter(sw);
            try {
                pw.writeObject(CryptoUtilities.getCertificate());
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Errore di IO o nella generazione del certificato");
            }
            String pemData = sw.toString();
            System.out.println(pemData);
            try {
                NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", pemData, textPlainContentType);
                } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class GetTrustedAuthorityCertificateTask extends AsyncTask<URL, Void, X509Certificate> {
        @Override
        protected X509Certificate doInBackground(URL... params) {
            URL searchUrl = params[0];
            String contactServerResult = null;
            X509Certificate certificate = null;
            try {
                contactServerResult = NetworkUtils.getResponseFromHttpUrl(searchUrl, "GET", "", textPlainContentType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                CertificateFactory fact = CertificateFactory.getInstance("X.509");
                certificate = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(contactServerResult.getBytes(Charset.forName("UTF-8"))));
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                Log.d(TAG, "Errore nella lettura del body ricevuto");
            } catch (CertificateException e2) {
                e2.printStackTrace();
                Log.d(TAG, "Errore nella creazione del certificato");
            }
            return certificate;
        }

        @Override
        protected void onPostExecute(X509Certificate x509Certificate) {
            if (x509Certificate != null) {
                System.out.println(x509Certificate.getSigAlgName());
                taCertificate = x509Certificate;
            } else
                Log.d(TAG, "Null certificate!");
            super.onPostExecute(x509Certificate);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
