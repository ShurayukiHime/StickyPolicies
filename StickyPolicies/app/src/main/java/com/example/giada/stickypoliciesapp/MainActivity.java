package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

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
                Context context = MainActivity.this;
                Class destinationActivity = XmlParserActivity.class;
                Intent startXmlParser = new Intent(context, destinationActivity);
                startActivity(startXmlParser);
            }
        });

        setContentView(R.layout.activity_main);

        mConnectToFacebookButton = (Button) findViewById(R.id.connect_to_facebook);
        mConnectToFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = MainActivity.this;
                Class destinationActivity = ConnectToFacebookActivity.class;
                Intent connectToSocial = new Intent(context, destinationActivity);
                startActivity(connectToSocial);
            }
        });
    }
}
