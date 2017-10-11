package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class ConnectToFacebookActivity extends AppCompatActivity {
    private LoginButton mFacebookLoginButton;
    private AccessToken accessToken;
    private CallbackManager callbackManager;
    private String TAG = "ConnectToFacebookActivi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_facebook);
        Log.d(TAG, "Entered the activity");

        //LoginManager.getInstance().logOut();

        mFacebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.KATANA_ONLY);
        mFacebookLoginButton.setReadPermissions("email");
        mFacebookLoginButton.setReadPermissions(Arrays.asList("user_photos"));
        mFacebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(ConnectToFacebookActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                accessToken = loginResult.getAccessToken();

                startNextActivity();
            }

            @Override
            public void onCancel() {
                Toast.makeText(ConnectToFacebookActivity.this, "CANCEL!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(ConnectToFacebookActivity.this, "ERROR!!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, error.toString());
            }
        });

        if (AccessToken.getCurrentAccessToken() != null) {
            Log.d(TAG, "Already logged in!");
            //LoginManager.getInstance().logOut();
            startNextActivity();
        }
    }

    private void startNextActivity () {
        Class nextActivity = DisplayFbProfileActivity.class;
        Intent intent = new Intent(ConnectToFacebookActivity.this, nextActivity);
        Log.d(TAG, "Let's go to next activity");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
