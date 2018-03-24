package com.example.giada.stickypoliciesapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE = 5555;
    private Button mSwitchToXmlParsingButton;
    private Button mServerPathButton;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwitchToXmlParsingButton = (Button) findViewById(R.id.switch_to_xml_parser);
        mSwitchToXmlParsingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Xml button Clicked!");
                Context context = MainActivity.this;
                Class destinationActivity = XmlParserActivity.class;
                Intent startXmlParser = new Intent(context, destinationActivity);
                startActivity(startXmlParser);
            }
        });

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
/*
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Permission not granted");
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Log.d(TAG, "Asking for permission...");

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_WRITE_EXT_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

                Log.d(TAG, "Permission granted");
            }
        } else {
            // Permission has already been granted

            Log.d(TAG, "Thanks!");
        }


        saveToInternalStorage();*/
    }

    private void saveToInternalStorage(){
        InputStream bm = getResources().openRawResource(R.raw.picture);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(bm);
        Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
        /*int nh = (int) (bmp.getHeight() * (512.0 / bmp.getWidth()));
        bmp = Bitmap.createScaledBitmap(bmp, 512, nh, true);
        view.setImageBitmap(bmp);*/

        /*ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"myPicture.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        //values.put(MediaStore.MediaColumns.DATA, filePath);

        this.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        String savedImageURL = MediaStore.Images.Media.insertImage(
                getContentResolver(),
                bmp,
                "Bird",
                "Image of bird"
        );
    }
}
