package com.example.giada.stickypoliciesapp;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giada.stickypoliciesapp.crypto.AsymmetricCryptoUtilities;
import com.example.giada.stickypoliciesapp.utilities.NetworkUtils;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

public class PolicyClient extends AppCompatActivity {

    private TextView mUrlDisplayTextView;
    private Button mObtainCertificateButton;
    private TextView mSearchResultsTextView;
    private Button mSendPiiButton;
    private Button mAccessPolicyButton;

    private String TAG = "PolicyClient";
    private JSONObject postData;

    final static String OBTAIN_CERT_PATH = "usrmgmt";
    final static String DATA_ACCESS_PATH = "plcmgmt";
    final static String DATA_SHARE_PATH = "plcmgmt";

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
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH);
        mUrlDisplayTextView.setText(serverURL.toString());

        //do something here

        // 1) separate encrypted PII from the rest
        // 2) contact TA and send stuff
        // 3) obtain key
        // 4) decrypt & profit

        new ContactTrustedAuthorityTask().execute(serverURL);
    }

    private void shareEncryptedData() {
        Log.d(TAG, "This is the share data function!");
        URL serverURL = NetworkUtils.buildUrl(DATA_SHARE_PATH);
        mUrlDisplayTextView.setText(serverURL.toString());

        // do something here:

        // 1) retrieve policy
        // 2) generate symmetric disposable encryption key
        // 3) encrypt policy
        // 4) hash policy
        // 5) concatenate policy and  digest, sign
        // 6) encrypt with TA's key




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

        new ContactTrustedAuthorityTask().execute(serverURL);
    }

    private void certificateRequest() {
        URL serverURL = NetworkUtils.buildUrl(OBTAIN_CERT_PATH);
        mUrlDisplayTextView.setText(serverURL.toString());

        // do something here:

        // 1) generate my own keys, if not available
        // 2) obtain TA's public key

        new GenerateAsymmetricKeysTask().execute();
        new ContactTrustedAuthorityTask().execute(serverURL);
    }

    public class ContactTrustedAuthorityTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            // generate certificate here hoping to lighten computation afterwards
            try {
                AsymmetricCryptoUtilities.getCertificate();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Errore nella generazione del certificato");
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String contactServerResult = null;
            Toast.makeText(getApplicationContext(), "Retrieving TA certificate...", Toast.LENGTH_LONG);
            try {
                contactServerResult = NetworkUtils.getResponseFromHttpUrl(searchUrl, "GET", "");
                Log.d(TAG, contactServerResult);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PEMParser parser = new PEMParser(new StringReader(contactServerResult));
            try {
                X509Certificate taCertificate = (X509Certificate) parser.readObject();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Errore nel riconoscimento del certificato ricevuto");
            }

            Toast.makeText(getApplicationContext(), "Generating Data Owner's certificate...", Toast.LENGTH_SHORT);

            StringWriter sw = new StringWriter();
            JcaPEMWriter pw = new JcaPEMWriter(sw);
            try {
                pw.writeObject(AsymmetricCryptoUtilities.getCertificate());
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Errore di IO o nella generazione del certificato");
            }
            String pemData = sw.toString();
            Toast.makeText(getApplicationContext(), "Sending Data Owner's certificate...", Toast.LENGTH_LONG);
            KeyPair keys = AsymmetricCryptoUtilities.getKeyPair();
            try {
                contactServerResult = NetworkUtils.getResponseFromHttpUrl(searchUrl, "POST", pemData);
                Log.d(TAG, contactServerResult);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return contactServerResult;
        }

        @Override
        protected void onPostExecute(String depositPolicyRequestResults) {
            if (depositPolicyRequestResults != null && !depositPolicyRequestResults.equals("")) {
                //mSearchResultsTextView.setText(githubSearchResults);
                mSearchResultsTextView.setText(Html.fromHtml(depositPolicyRequestResults));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class GenerateAsymmetricKeysTask extends AsyncTask <Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            AsymmetricCryptoUtilities.generateKeys();
            return null;
        }
        // integer may be used to show a progress bar
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
