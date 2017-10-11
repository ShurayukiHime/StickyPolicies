package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giada on 13/09/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private AccessToken accessToken;
    private ArrayList<String> urls;
    private String[] mThumbUrls;
    //private String[] mThumbIds;
    private String TAG = "ImageAdapter";

    public ImageAdapter(Context c) {
        this.mContext = c;
        this.accessToken = AccessToken.getCurrentAccessToken();
        this.urls = new ArrayList<String>();
        this.mThumbUrls = new String[0];

        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/me/photos",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject result = response.getJSONObject();
                        try {
                            JSONArray data = result.getJSONArray("data");
                            new FetchImagesTask().execute(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("type", "uploaded");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public int getCount() {
        return mThumbUrls.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private File[] getAllFiles (String directoryPath) {
        File dir = new File (directoryPath);
        return dir.listFiles();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        Log.d(TAG, "getView called");

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        //File[] paths = this.getAllFiles(Environment.DIRECTORY_PICTURES + R.string.internal_storage_directory);
        String[] paths = mThumbUrls;
        //Log.d(TAG, paths.length + "");
        //for (int i = 0; i < paths.length; i++) {
            //Picasso.with(mContext).load(paths[position]).into(imageView);

            Picasso.with(mContext).load(mThumbUrls[position]).into(imageView);
        Log.d(TAG, mThumbUrls[position]);
        //}
        return imageView;
    }

    public class FetchImagesTask extends AsyncTask<JSONArray, Void, Void>  {

        @Override
        protected Void doInBackground(JSONArray... params) {
            JSONArray ids = params[0];
            //mThumbIds = new String[ids.length()];

            for (int i = 0; i < ids.length(); i++) {
                try {
                    final String id = ids.getJSONObject(i).getString("id");
                    //mThumbIds[i] = id;

                    GraphRequest request = GraphRequest.newGraphPathRequest(
                            accessToken,
                            id,
                            new GraphRequest.Callback() {
                                @Override
                                public void onCompleted(GraphResponse response) {
                                    JSONObject result = response.getJSONObject();
                                    try {
                                        JSONArray data = result.getJSONArray("images");
                                        String urlSource = data.getJSONObject(0).getString("source");
                                        urls.add(urlSource);
                                        mThumbUrls = urls.toArray(new String[urls.size()]);
                                        Log.d(TAG, "Aggiornato array URLs: " + id);
                                        notifyDataSetChanged();

                                        // QUESTA COSA QUI NON VA BENE!!! SI FANNO TROPPE CHIAMATE INUTILI!!
                                        // PERò NON SO COME CAMBIARLO, OVUNQUE ALTRO LO METTO NON FUNZIONA

                                        //saveUrlsToInternalStorage();
                                        //Log.d(TAG, "Foto salvate!");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "images");
                    request.setParameters(parameters);
                    request.executeAsync();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

/*    private void saveUrlsToInternalStorage() {
        //there may be issues with saving all pictures to internal storage, but for now I have no better idea

        for (int i = 0; i < mThumbIds.length; i++) {
            final int index = i;

            String fileName = mThumbIds[index] + ".jpg";
            File myDir = new File (Environment.DIRECTORY_PICTURES + R.string.internal_storage_directory);

            File myFile = new File(myDir, fileName);
            if (myFile.exists()) {
                Log.d(TAG, "Image already downloaded! No need to download it another time.");
                return;
            } //else

            Picasso.with(mContext)
                    .load(mThumbUrls[i])
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            String directory = Environment.DIRECTORY_PICTURES;
                            String fileName = mThumbIds[index] + ".jpg";

                            File myDir = new File(directory + R.string.internal_storage_directory);
                            if (!myDir.exists())
                                myDir.mkdir();

                            File myFile = new File(myDir, fileName);
                            try {
                                FileOutputStream fout = new FileOutputStream(myFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);

                                fout.flush();
                                fout.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }
    }*/
}