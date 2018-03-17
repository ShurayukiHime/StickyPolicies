package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class XmlParserActivity extends AppCompatActivity {

    private TextView mXmlParsingResults;
    private Button mStartParsingButton;
    private static XmlParserActivity activityInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xml_parser);
        activityInstance = this;

        mXmlParsingResults = (TextView) findViewById(R.id.xml_parsing_results);
        mStartParsingButton = (Button) findViewById(R.id.start_parsing);
        mStartParsingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startParsing();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private static Context getContext() {
        return activityInstance;
    }

    private void startParsing() throws IOException, XmlPullParserException {
        /*String filename ="/bozza xml policy.txt";
        File policyFile = getContext().getFileStreamPath(filename);

        File file = XmlParserActivity.getContext().getFileStreamPath("/mnt/sdcard/photo/1342147146535.jpg");
        if(file.exists()){
            Toast.makeText(XmlParserActivity.getContext(), "File exists in /mnt", Toast.LENGTH_SHORT);
        }

        Log.d("XmlParserActivity", policyFile.getAbsolutePath());
        if (policyFile == null || !policyFile.exists()) {
            Toast.makeText(this, "File does not exist!!", Toast.LENGTH_SHORT).show();
            return;
        }*/

        InputStream is = getContext().getResources().openRawResource(R.raw.policy1);

        mXmlParsingResults.setText("");
        StickyPolicyParser parser = new StickyPolicyParser();
        List<StickyPolicyParser.Entry> entries = null;
        try {
            entries = parser.parse(is);
        }finally {
            is.close();
        }
        for (StickyPolicyParser.Entry e : entries) {
            mXmlParsingResults.append(e.toString());
        }
    }


}
