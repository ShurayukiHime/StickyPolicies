package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mServerPathButton;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServerPathButton = (Button) findViewById(R.id.share_pii_button);
        mServerPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Share_data button Clicked!");
                Context context = MainActivity.this;
                Class destinationActivity = ShareDataActivity.class;
                Intent sharePii = new Intent(context, destinationActivity);
                startActivity(sharePii);
            }
        });
    }
}
