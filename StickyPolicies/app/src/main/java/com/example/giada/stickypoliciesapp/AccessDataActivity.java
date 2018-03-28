package com.example.giada.stickypoliciesapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.giada.stickypoliciesapp.utilities.CryptoUtils;
import com.example.giada.stickypoliciesapp.utilities.NetworkUtils;
import com.google.gson.Gson;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class AccessDataActivity extends AppCompatActivity {
    private final static String TAG = AccessDataActivity.class.getSimpleName();
    private EncryptedData bobsData;

    final static String DATA_ACCESS_PATH = "access";

    private TextView mUrlDisplayTextView;
    private TextView mSearchResultsTextView;
    private int cipher_block_length = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_data);

        mSearchResultsTextView = (TextView) findViewById(R.id.tv_search_results);
        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);

        Bundle extrasBundle = getIntent().getExtras();
        Gson gson = new Gson();
        if (!extrasBundle.isEmpty() && extrasBundle.containsKey("string_gson_data")){
            bobsData = gson.fromJson((String) extrasBundle.get("string_gson_data"), EncryptedData.class);
        }

        accessEncryptedData();
    }

    private void accessEncryptedData() {
        // suppose we received bobsData as Gson from Alice

        // 2) contact TA and send stuff
        EncryptedData taData = new EncryptedData(bobsData.getStickyPolicy(), bobsData.getKeyAndHashEncrypted(), bobsData.getSignedEncrkeyAndHash(), null);
        Gson jsonData = new Gson();
        String postData = jsonData.toJson(taData, EncryptedData.class);
        URL serverURL = NetworkUtils.buildUrl(DATA_ACCESS_PATH, "", "");
        mUrlDisplayTextView.setText(serverURL.toString());
        // 3) obtain key

        //POSSIBLE IMPROVEMENT: BOB REGISTERS@TA AND KEY IS SENT ENCRYPTED WITH BOB'S PUBK
        //MAY NOT BE A GOOD IDEA IF DATA IS LARGER THAN MODULUS?

        byte[] encodedSymmKey = new byte[0];
        try {
            encodedSymmKey = new PolicyClient.SendEncryptedDataTask(postData).execute(serverURL).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.d(TAG, "Error or refusal from Trusted Authority: " + e.getMessage());
        }
        //may not be the wisest solution
        // 4) decryptSymmetric & profit
        try {
            byte[] encrPiiAndIV = bobsData.getEncrPiiAndIV();
            byte[] encryptedPii = Arrays.copyOfRange(encrPiiAndIV, 0, (encrPiiAndIV.length - cipher_block_length));
            byte[] initVec = Arrays.copyOfRange(encrPiiAndIV, (encrPiiAndIV.length - cipher_block_length), encrPiiAndIV.length);
            byte[] decodedPii = CryptoUtils.decryptSymmetric(encodedSymmKey, initVec, encryptedPii);
            String pii = new String (decodedPii, Charset.forName("UTF-8"));
            mSearchResultsTextView.append("Alice's personal data: " + pii + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error in decrypting personal data: " + e.getMessage());
            mSearchResultsTextView.append("We're sorry, something went wrong.");
        }
    }
}
