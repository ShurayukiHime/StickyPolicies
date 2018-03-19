package com.example.giada.stickypoliciesapp;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giada.stickypoliciesapp.utilities.CryptoUtils;
import com.example.giada.stickypoliciesapp.utilities.NetworkUtils;
import com.google.gson.Gson;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
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

    private final static String TAG = PolicyClient.class.getSimpleName();

    final static String OBTAIN_CERT_PATH = "certificates";
    final static String DATA_ACCESS_PATH = "access";
    final static String textPlainContentType = "text/plain; charset=utf-8";
    final static String applicationJsonContentType = "application/json; charset=utf-8";

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

            //PAY ATTENTION TO FILENAME!
            fileInputStream = this.getApplicationContext().openFileInput("somefile");
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
        String policyFile = null;
        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier(fileName, "raw", getPackageName()));
        Scanner scanner = new Scanner(ins);
        scanner.useDelimiter("\\A");
        boolean hasInput = scanner.hasNext();
        if (hasInput)
            policyFile = scanner.next();
        else
            Log.d(TAG, "Empty file");
        try {
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error closing input stream: " + e.getMessage());
        }

        Key taPublicKey = null;
        if(taCertificate == null) {
            Log.d(TAG, "Certificate is null, creating a new one...");
            Toast.makeText(this.getApplicationContext(), "Please wait... Cryptography is working for you!", Toast.LENGTH_LONG);
            URL serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "action", "obtainTAcertificate");
            mUrlDisplayTextView.setText(serverURL.toString());
            try {
                taCertificate = new GetTrustedAuthorityCertificateTask().execute(serverURL).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in retrieving certificate: " + e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in retrieving certificate: " + e.getMessage());
            }
        } else
            Log.d(TAG, "Certificate not null");
        taPublicKey = taCertificate.getPublicKey();


        BigInteger dataOwnerCertSN = null;
        //to execute this task I would normally need only my keypair, not the entire certificate
        //but since I have to specify a SN in the policy, then also the certificate is generated
        try {
            X509Certificate dataOwnerCert = CryptoUtils.getCertificate();
            dataOwnerCertSN = dataOwnerCert.getSerialNumber();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error when generating Data Owner's certificate");
        }
        ////////////////////////////////////////
        ///////////// ---- HARDCODED -- NOT GOOD
        String[] fakeSN = policyFile.split("certificateSerialNumber");
        String policy = fakeSN[0] + "certificateSerialNumber>" + dataOwnerCertSN + "</certificateSerialNumber" + fakeSN[2];
        Log.d(TAG, "Serial  number: " + dataOwnerCertSN);
        ///////////// ---- HARDCODED -- NOT GOOD
        ////////////////////////////////////////

        // 2) generate symmetric disposable encryption key
        byte[] encodedSymmetricKey = CryptoUtils.generateSymmetricRandomKey();
        // 3) encrypt PII
        byte[] encryptedPii = CryptoUtils.encryptSymmetric(encodedSymmetricKey, pii.getBytes(Charset.forName("UTF-8")));
        // 4) hash policy
        byte[] policyDigest = new byte[0];
        try {
            policyDigest = CryptoUtils.calculateDigest(policy.getBytes(Charset.forName("UTF-8")));
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
        byte[] keyAndHashEncrypted = CryptoUtils.encryptAsymmetric(taPublicKey, byteArrayOutputStream.toByteArray());

        // 6) sign
        byte[] signedEncrKeyAndHash = new byte[0];
        try {
            signedEncrKeyAndHash = CryptoUtils.sign(keyAndHashEncrypted);
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
        EncryptedData data = new EncryptedData(policy, keyAndHashEncrypted, signedEncrKeyAndHash, encryptedPii);
        Gson jsonData = new Gson();
        String postData = jsonData.toJson(data, EncryptedData.class);
        System.out.println("\n" + postData + "\n");
        // this url should be changed with Bob's address
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        new SendEncryptedDataTask(postData).execute(serverURL);
    }

    private class EncryptedData {
        private String stickyPolicy;
        private byte[] keyAndHashEncrypted;
        private byte[] signedEncrkeyAndHash;
        private byte[] encryptedPii;

        public EncryptedData() {
        }

        public EncryptedData(String stickyPolicy, byte[] keyAndHashEncrypted, byte[] signedEncrkeyAndHash, byte[] encryptedPii) {
            this.stickyPolicy = stickyPolicy;
            this.keyAndHashEncrypted = keyAndHashEncrypted;
            this.signedEncrkeyAndHash = signedEncrkeyAndHash;
            this.encryptedPii = encryptedPii;
        }

        public String getStickyPolicy() {
            return stickyPolicy;
        }

        public void setStickyPolicy(String stickyPolicy) {
            this.stickyPolicy = stickyPolicy;
        }

        public byte[] getKeyAndHashEncrypted() {
            return keyAndHashEncrypted;
        }

        public void setKeyAndHashEncrypted(byte[] keyAndHashEncrypted) {
            this.keyAndHashEncrypted = keyAndHashEncrypted;
        }

        public byte[] getSignedEncrkeyAndHash() {
            return signedEncrkeyAndHash;
        }

        public void setSignedEncrkeyAndHash(byte[] signedEncrkeyAndHash) {
            this.signedEncrkeyAndHash = signedEncrkeyAndHash;
        }

        public byte[] getEncryptedPii() {
            return encryptedPii;
        }

        public void setEncryptedPii(byte[] encryptedPii) {
            this.encryptedPii = encryptedPii;
        }
    }

    private void certificateRequest() {
        URL serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "action", "obtainTAcertificate");
        mUrlDisplayTextView.setText(serverURL.toString());
        new GetTrustedAuthorityCertificateTask().execute(serverURL);

        serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        new SendMyCertificateTask().execute(serverURL);
    }

    public class SendEncryptedDataTask extends  AsyncTask<URL, Void, String> {
        String postData;

        protected SendEncryptedDataTask(String postData) {
            this.postData = postData;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null)
                mSearchResultsTextView.append("Result sending data: " + s + "\n");
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(URL... urls) {
            String responseBody = null;
            URL searchUrl = urls[0];
            try {
                responseBody = NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", postData, applicationJsonContentType);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error when sending encrypted POST: " + e.getMessage());
            }
            return responseBody;
        }
    }

    public class SendMyCertificateTask extends  AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            // generate certificate here hoping to lighten computation afterwards
            try {
                CryptoUtils.getCertificate();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Error when generating Data Owner's certificate");
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null)
                mSearchResultsTextView.append("Data Owner Certificate SN: " + s + "\n");
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL searchUrl = urls[0];
            StringWriter sw = new StringWriter();
            JcaPEMWriter pw = new JcaPEMWriter(sw);
            String certificateSN = null;
            try {
                pw.writeObject(CryptoUtils.getCertificate());
                certificateSN = CryptoUtils.getCertificate().getSerialNumber() + "";
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "IO Exception or Error generating the certificate");
            }
            String pemData = sw.toString();
            try {
                NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", pemData, textPlainContentType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return certificateSN;
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
                Log.d(TAG, "Error reagin received response body");
            } catch (CertificateException e2) {
                e2.printStackTrace();
                Log.d(TAG, "Error while creating certificate");
            }
            return certificate;
        }

        @Override
        protected void onPostExecute(X509Certificate x509Certificate) {
            if (x509Certificate != null) {
                taCertificate = x509Certificate;
                mSearchResultsTextView.append("Trusted Authority's certificate SN: " + x509Certificate.getSerialNumber() + "\n");
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
