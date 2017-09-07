package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mSwitchToXmlParsingButton;
    private Button mConnectToFacebookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitchToXmlParsingButton = (Button) findViewById(R.id.switch_to_xml_parser);
        mSwitchToXmlParsingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Xml button Clicked!");
                Context context = MainActivity.this;
                Class destinationActivity = XmlParserActivity.class;
                Intent startXmlParser = new Intent(context, destinationActivity);
                startActivity(startXmlParser);
            }
        });

        mConnectToFacebookButton = (Button) findViewById(R.id.connect_to_facebook);
        mConnectToFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MainActivity", "Facebook button Clicked!");
                Context context = MainActivity.this;
                Class destinationActivity = ConnectToFacebookActivity.class;
                Intent connectToSocial = new Intent(context, destinationActivity);
                startActivity(connectToSocial);
            }
        });
    }
}