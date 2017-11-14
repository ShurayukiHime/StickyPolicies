package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giada.stickypoliciesapp.utilities.NetworkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

public class PolicyClient extends AppCompatActivity {

    private TextView mUrlDisplayTextView;
    private Button mUserManagementButton;
    private TextView mSearchResultsTextView;

    private String TAG = "PolicyClient";
    private String policy = "this is a cool policy!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy_client);
        Log.d(TAG, "entered policy client!");

        mUrlDisplayTextView = (TextView) findViewById(R.id.tv_url_display);
        mSearchResultsTextView = (TextView) findViewById(R.id.tv_github_search_results_json);

        mUserManagementButton = (Button) findViewById(R.id.user_management_button);
        mUserManagementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("PolicyClient", "usrmgmt button Clicked!");
                depositPolicyRequest();
            }
        });
    }

    private void depositPolicyRequest() {
        String githubQuery = ""; //eventually insert a textbox and retrieve this value from user input
        URL serverURL = NetworkUtils.buildUrl(githubQuery);
        mUrlDisplayTextView.setText(serverURL.toString());

        String policyfile = "/PolicyFolder/policy1.xml";
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
        }
        new DepositPolicyTask().execute(serverURL);
    }

    public class DepositPolicyTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            String depositPolicyRequestResults = null;
            try {
                depositPolicyRequestResults = NetworkUtils.getResponseFromHttpUrl(searchUrl, policy);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return depositPolicyRequestResults;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemThatWasClickedId = item.getItemId();
        if (itemThatWasClickedId == R.id.user_management_button) {
            depositPolicyRequest();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
