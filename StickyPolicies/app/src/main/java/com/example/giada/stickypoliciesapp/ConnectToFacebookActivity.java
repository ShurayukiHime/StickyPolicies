package com.example.giada.stickypoliciesapp;

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
    private Button mInvalidateSessionButton;
    private CallbackManager callbackManager;
    private String TAG = "ConnectToFacebookActivi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_facebook);
        Log.d(TAG, "Entered the activity");

        mInvalidateSessionButton = (Button) findViewById(R.id.invalidate_session);
        mInvalidateSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // https://graph.accountkit.com/v1.2/logout?access_token={user_access_token}
                LoginManager.getInstance().logOut();
            }
        });

        mFacebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        mFacebookLoginButton.setReadPermissions("email");

        callbackManager = CallbackManager.Factory.create();
        mFacebookLoginButton.setReadPermissions(Arrays.asList("user_photos"));
        mFacebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(ConnectToFacebookActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,GraphResponse response) {
                                try {
                                    Toast.makeText(ConnectToFacebookActivity.this, "Logged in as " + object.getString("name"), Toast.LENGTH_SHORT).show();
                                    String id = object.getString("id");
                                    String name = object.getString("name");
                                    String link = object.getString("link");
                                } catch(JSONException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link");
                request.setParameters(parameters);
                request.executeAsync();

                Class nextActivity = DisplayFbProfileActivity.class;
                Intent intent = new Intent(ConnectToFacebookActivity.this, nextActivity);
                startActivity(intent);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
