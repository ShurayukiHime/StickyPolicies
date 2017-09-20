package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONException;
import org.json.JSONObject;

public class DisplayFbProfileActivity extends AppCompatActivity {

    private ProfilePictureView mProfilePicture;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private AccessToken accessToken;
    private String TAG = "DisplayFbProfileActivit";

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Going back to MainActivity");
        Context context = DisplayFbProfileActivity.this;
        Class destinationActivity = MainActivity.class;
        Intent startXmlParser = new Intent(context, destinationActivity);
        startActivity(startXmlParser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_fb_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        accessToken = AccessToken.getCurrentAccessToken(); //controllare se null?

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(new ImageAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(DisplayFbProfileActivity.this, "" + position,
                        Toast.LENGTH_SHORT).show();
            }
        });


        mCollapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        //mProfilePicture = (ProfilePictureView) findViewById(R.id.userProfilePicture);
        //mProfilePicture.setProfileId(accessToken.getUserId());

        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            Toast.makeText(DisplayFbProfileActivity.this, "Logged in as " + object.getString("name"), Toast.LENGTH_SHORT).show();
                            mCollapsingToolbar.setTitle(object.getString("name"));
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
