package com.example.giada.stickypoliciesapp;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.giada.stickypoliciesapp.crypto.CryptoUtilities;
import com.example.giada.stickypoliciesapp.utilities.NetworkUtils;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.DigestException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

public class PolicyClient extends AppCompatActivity {

    private TextView mUrlDisplayTextView;
    private Button mObtainCertificateButton;
    private TextView mSearchResultsTextView;
    private Button mSendPiiButton;
    private Button mAccessPolicyButton;

    private String TAG = "PolicyClient";
    private JSONObject postData;

    final static String OBTAIN_CERT_PATH = "certificates";
    final static String DATA_ACCESS_PATH = "access";
    final static String DATA_SHARE_PATH = "plcmgmt";

    private X509Certificate taCertificate;

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
        Log.d(TAG, "This is the access data function!");
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH, "","");
        mUrlDisplayTextView.setText(serverURL.toString());

        //do something here

        // 1) separate encrypted PII from the rest
        // 2) contact TA and send stuff
        // 3) obtain key
        // 4) decryptSymmetric & profit

        new GetTrustedAuthorityCertificateTask().execute(serverURL);
    }

    private void shareEncryptedData() {
        Log.d(TAG, "This is the share data function!");
        URL serverURL = NetworkUtils.buildUrl(DATA_SHARE_PATH, "","" );
        mUrlDisplayTextView.setText(serverURL.toString());

         // 1) retrieve policy
        // 2) generate symmetric disposable encryption key
        byte[] encodedSymmetricKey = CryptoUtilities.generateSymmetricRandomKey();
        String supposeThisIsAPolicy = "Heeeeeeyyyyy";
        // 3) encryptSymmetric policy
        byte[] encodedPolicy = CryptoUtilities.encryptSymmetric(encodedSymmetricKey, supposeThisIsAPolicy.getBytes(Charset.forName("UTF-8")));
        // 4) hash policy
        byte[] policyDigest = new byte[0];
        try {
            policyDigest = CryptoUtilities.calculateDigest(supposeThisIsAPolicy.getBytes(Charset.forName("UTF-8")));
        } catch (DigestException e) {
            e.printStackTrace();
            Log.d(TAG, "Error calculating policy digest: " + e.getMessage());
        }
        // 5) concatenate policy and  digest, sign
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write(supposeThisIsAPolicy.getBytes(Charset.forName("UTF-8")));
            outputStream.write(policyDigest);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error in concatenating policy and digest: " + e.getMessage());
        }
        byte[] signedPolicyAndDigest = new byte[0];
        try {
            signedPolicyAndDigest = CryptoUtilities.sign(outputStream.toByteArray());
        } catch (SignatureException e) {
            e.printStackTrace();
            Log.d(TAG, "Error when signing data: " + e.getMessage());
        }
        // 6) encryptSymmetric with TA's key
        Key publicKey = taCertificate.getPublicKey();
        byte[] encryptedSignedMessage = CryptoUtilities.encryptAsymmetric(publicKey, signedPolicyAndDigest);

        // save or share somewhere somehow


        /*        String policyfile = "/PolicyFolder/policy1.xml";
        String filePath = Environment.getExternalStorageDirectory() + policyfile;
        File f = new File(filePath);
        if (f.exists()) {
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;

                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                //You'll need to add proper error handling here
                Log.d(TAG, e.getLocalizedMessage());
            }
            policy = sb.toString();
        } else {
            Toast.makeText(this, "File doesn't exist :(", Toast.LENGTH_LONG);
        }*/

        postData = new JSONObject();
        try {
            postData.put("dataOwner", "TizioCaio");
            postData.put("policy", "Full disclose");
            postData.put("encoding", "sd345ggsdrUJ%%l£km3Nnk");
        }
        catch(Exception e){
            e.printStackTrace();
        }

        new GetTrustedAuthorityCertificateTask().execute(serverURL);
    }

    private void certificateRequest() {
        URL serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "action", "obtainTAcertificate");
        mUrlDisplayTextView.setText(serverURL.toString());
        new GetTrustedAuthorityCertificateTask().execute(serverURL);

        serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        new SendMyCertificateTask().execute(serverURL);
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
            String contactServerResult = null;
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
                contactServerResult = NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", pemData);
                Log.d(TAG, contactServerResult);
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
                contactServerResult = NetworkUtils.getResponseFromHttpUrl(searchUrl, "GET", "");
            } catch (IOException e) {
                e.printStackTrace();
            }
            String body = NetworkUtils.extractBody(contactServerResult);
            try {
                String begin = "/r/n----BEGIN CERTIFICATE-----/r/n";
                String end = "/r/n-----END CERTIFICATE-----/r/n/r/n";
                String temp = body.substring(begin.length(), (body.length() - end.length()));
                String parsedBody = temp.replaceAll("\\p{javaSpaceChar}{1,}", "\r\n");
                String finalCert = begin.concat(parsedBody.concat(end));
                PEMParser parser = new PEMParser(new StringReader(finalCert));
                //PEMParser parser = new PEMParser(new StringReader(body));
                certificate = (X509Certificate) parser.readObject();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Errore nel riconoscimento del certificato ricevuto\n" + e.getMessage());
            } catch (NullPointerException e1) {
                e1.printStackTrace();
                Log.d(TAG, "Errore nella lettura del body ricevuto");
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

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.user_management_button) {
            certificateRequest();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/
}
