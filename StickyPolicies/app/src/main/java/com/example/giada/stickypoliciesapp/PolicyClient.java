package com.example.giada.stickypoliciesapp;


import android.content.Context;
import android.content.Intent;
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
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutionException;

public class PolicyClient extends AppCompatActivity {

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;
    private TextView mDataShared;
    private Button mAccessDataButton;

    private final static String TAG = PolicyClient.class.getSimpleName();

    final static String OBTAIN_CERT_PATH = "certificates";
    final static String DATA_ACCESS_PATH = "access";
    final static String textPlainContentType = "text/plain; charset=utf-8";
    final static String applicationJsonContentType = "application/json; charset=utf-8";

    private X509Certificate taCertificate;
    private String pii;
    private String policy;
    private EncryptedData bobsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_client);
        Log.d(TAG, "entered policy client!");

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_search_results);
        mDataShared = (TextView) findViewById(R.id.data_shared_title);

        mAccessDataButton = (Button) findViewById(R.id.access_data_button);
        mAccessDataButton.setClickable(false);
        mAccessDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "access encrypted data button Clicked!");
                accessEncryptedData();
            }
        });

        Bundle extrasBundle = getIntent().getExtras();
        if (!extrasBundle.isEmpty() && extrasBundle.containsKey("string_pii")){
            pii = (String) extrasBundle.get("string_pii");
        }
        if (!extrasBundle.isEmpty() && extrasBundle.containsKey("policy")){
            policy = (String) extrasBundle.get("policy");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        shareEncryptedData();
        Toast.makeText(this.getApplicationContext(), "Please wait... Data is being shared...", Toast.LENGTH_SHORT).show();
    }

    private void accessEncryptedData() {
        Context context = PolicyClient.this;
        Class destinationActivity = AccessDataActivity.class;
        Intent accessDataIntent = new Intent(context, destinationActivity);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("string_gson_data", gson.toJson(bobsData, EncryptedData.class));
        accessDataIntent.putExtras(bundle);
        startActivity(accessDataIntent);
    }

    private void shareEncryptedData() {
        URL serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        new SendMyCertificateTask().execute(serverURL);

        // 1) retrieve policy
            // policy already retrieved in previous activity

        byte[] encryptedPii = new byte[0];
        byte[] keyAndHashEncrypted = new byte[0];
        byte[] signedEncrKeyAndHash = new byte[0];
        try {
            PublicKey taPublicKey = null;
            serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "action", "obtainTAcertificate");
            mUrlDisplayTextView.setText(serverURL.toString());
            try {
                taCertificate = new GetTrustedAuthorityCertificateTask().execute(serverURL).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in retrieving certificate: " + e.getMessage());
                throw new Exception();
            } catch (ExecutionException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in retrieving certificate: " + e.getMessage());
                throw new Exception();
            }
            taPublicKey = taCertificate.getPublicKey();

            // 2) generate symmetric disposable encryption key
            byte[] encodedSymmetricKey = CryptoUtils.generateSymmetricRandomKey();
            // 3) encrypt PII
            encryptedPii = CryptoUtils.encryptSymmetric(encodedSymmetricKey, pii.getBytes(Charset.forName("UTF-8")));
            // 4) hash policy
            byte[] policyDigest = new byte[0];
            try {
                policyDigest = CryptoUtils.calculateDigest(policy.getBytes(Charset.forName("UTF-8")));
            } catch (DigestException e) {
                e.printStackTrace();
                Log.d(TAG, "Error calculating policy digest: " + e.getMessage());
                throw new Exception ();
            }
            // 5) append policy and  digest, encrypt with PubTa
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( );
            try {
                byteArrayOutputStream.write(encodedSymmetricKey);
                byteArrayOutputStream.write(policyDigest);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error in concatenating policy and digest: " + e.getMessage());
                throw new Exception ();
            }
            keyAndHashEncrypted = CryptoUtils.encryptAsymmetric(taPublicKey, byteArrayOutputStream.toByteArray());

            // 6) sign
            signedEncrKeyAndHash = new byte[0];
            try {
                signedEncrKeyAndHash = CryptoUtils.sign(keyAndHashEncrypted);
            } catch (SignatureException e) {
                e.printStackTrace();
                Log.d(TAG, "Error when signing data: " + e.getMessage());
                throw new Exception ();
            }
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error when closing byte array output stream: " + e.getMessage());
                throw new Exception ();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mSearchResultsTextView.setText("Sorry, some errors happened while encrypting your data.\nDon't worry, your privacy is still safe!\nPlease try again later.");
            return;
            // don't think this is the brightest idea ever...
        }

        // save or share somewhere somehow

        bobsData = new EncryptedData(policy, keyAndHashEncrypted, signedEncrKeyAndHash, encryptedPii);
        // TA doesn't receive pii
            //1) bc the paper says so
            //2) bc it'd introduce a weakness

        // this part has been commented out because the TA shouldn't receive any data from the data owner
        // it would make sense to integrate it back if we had a real bob and send it to him!
        // anyway the code works pretty fine so it was a waste to delete it

        /*EncryptedData data = new EncryptedData(policy, keyAndHashEncrypted, signedEncrKeyAndHash, null);
        Gson jsonData = new Gson();
        String postData = jsonData.toJson(data, EncryptedData.class);
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        new SendEncryptedDataTask(postData).execute(serverURL)*/;

        // since we don't send it to Bob, we pass the data to the new activity
        mDataShared.setVisibility(View.VISIBLE);
        mAccessDataButton.setClickable(true);
    }

    public static class SendEncryptedDataTask extends  AsyncTask<URL, Void, byte[]> {
        String postData;

        protected SendEncryptedDataTask(String postData) {
            this.postData = postData;
        }

        @Override
        protected byte[] doInBackground(URL... urls) {
            String responseBody = null;
            URL searchUrl = urls[0];
            try {
                responseBody = NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", postData, applicationJsonContentType);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Error when sending encrypted POST: " + e.getMessage());
            }
            Gson gson = new Gson();
            return gson.fromJson(responseBody, byte[].class);
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
        }
    }

    public class SendMyCertificateTask extends  AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            // should have already been generated in the previous activity
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
