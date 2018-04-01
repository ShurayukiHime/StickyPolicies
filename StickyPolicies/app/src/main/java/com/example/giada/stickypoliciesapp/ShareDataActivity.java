package com.example.giada.stickypoliciesapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.giada.stickypoliciesapp.utilities.CryptoUtils;
import com.example.giada.stickypoliciesapp.utilities.ParsingUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.Scanner;

public class ShareDataActivity extends AppCompatActivity {

    private Button browseButton;
    private Button mConfirmButton;
    private TextView mTextToShare;
    private ImageView mImageView;
    private static final String TAG = ShareDataActivity.class.getSimpleName();
    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_data);

        mImageView = (ImageView) findViewById(R.id.selected_image_share);
        browseButton = (Button) findViewById(R.id.browse_internal_storage_button);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Browse button Clicked!");
                performFileSearch();
            }
        });

        mTextToShare = (TextView) findViewById(R.id.manual_insert_share_data);

        mConfirmButton = (Button) findViewById(R.id.confirm_share_button);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Browse button Clicked!");

                // generate certificate
                X509Certificate dataOwnerCert = null;
                try {
                    dataOwnerCert = CryptoUtils.getCertificate();
                } catch (Exception e) {
                    Log.d(TAG, "Error generating the data owner's certificate.");
                    e.printStackTrace();
                }

                // generate policy
                String policyFile = readPolicy();
                String policy = ParsingUtils.correctPolicyFile(policyFile, ParsingUtils.CERT_SN_TAG, dataOwnerCert.getSerialNumber() + "");

                Context context = ShareDataActivity.this;
                Class destinationActivity = PolicyClient.class;
                Intent sharePii = new Intent(context, destinationActivity);
                Bundle bundle = new Bundle();

                // needs generalization
                BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();

                if (drawable != null) {
                    Bitmap bitmap = drawable.getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    bundle.putByteArray("image_pii", byteArray);
                    policy = ParsingUtils.correctPolicyFile(policy, ParsingUtils.DATA_TYPE_TAG, ParsingUtils.DATA_TYPE_PICTURE);
                    bundle.putString("policy", policy);
                } else{
                    String text = mTextToShare.getText() + "";
                    if (!text.isEmpty()) {
                        bundle.putString("string_pii", mTextToShare.getText() + "");
                        policy = ParsingUtils.correctPolicyFile(policy, ParsingUtils.DATA_TYPE_TAG, ParsingUtils.DATA_TYPE_TEXT);
                        bundle.putString("policy", policy);
                    }
                }
                sharePii.putExtras(bundle);
                startActivity(sharePii);
            }
        });
    }

    private String readPolicy() {
        String fileName = "policy1";
        String policyFile = null;
        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier(fileName, "raw", getPackageName()));
        Scanner scanner = new Scanner(ins);
        scanner.useDelimiter("\\A");
        boolean hasInput = scanner.hasNext();
        if (hasInput)
            policyFile = scanner.next();
        else
            Log.d(TAG, "Empty file");
        try {
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error closing input stream: " + e.getMessage());
        }
        return policyFile;
    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                try {
                    Bitmap image = getBitmapFromUri(uri);
                    mImageView.setImageBitmap(image);
                } catch (IOException e) {
                    Log.d(TAG, "Error when opening the bitmap image.");
                    e.printStackTrace();
                }

            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
