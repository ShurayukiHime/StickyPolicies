package com.example.giada.stickypoliciesapp;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Giada on 13/09/2017.
 */

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private AccessToken accessToken;
    private ArrayList<String> urls;
    private String[] mThumbUrls;
    private String TAG = "ImageAdapter";


    public ImageAdapter(Context c) {
        this.mContext = c;
        this.accessToken = AccessToken.getCurrentAccessToken();
        this.urls = new ArrayList<String>();
        this.mThumbUrls = new String[0];

        if (this.mThumbUrls == null) {
            Log.d(TAG, "null Array!");
        } else {
            Log.d(TAG, "" + this.mThumbUrls.length);
        }

        GraphRequest request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/me/photos",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject result = response.getJSONObject();
                        if (result == null)
                            Log.d(TAG, "No Object!");
                        else {
                            try {
                                JSONArray data = result.getJSONArray("data");
                                for (int i = 0; i < data.length(); i++) {
                                    String id = data.getJSONObject(i).getString("id");
                                    Log.d(TAG, id);
                                    new FetchImagesTask().execute(id);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        Log.d(TAG, "getView called!");

        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        String url = mThumbUrls[position];
        Log.d(TAG, position + " " + url);
        Picasso.with(mContext).load(url).into(imageView);
        return imageView;

    }

    public class FetchImagesTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String id = params[0];

            GraphRequest request = GraphRequest.newGraphPathRequest(
                    accessToken,
                    id,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            Log.d("FetchImagesAsyncTask", "completed one request for image");
                            JSONObject result = response.getJSONObject();
                            if (result == null)
                                Log.d(TAG, "No Object!");
                            else {
                                try {
                                    JSONArray data = result.getJSONArray("images");
                                    String urlSource = data.getJSONObject(data.length() - 1).getString("source");
                                    urls.add(urlSource);
                                    mThumbUrls = urls.toArray(new String[urls.size()]);
                                    notifyDataSetChanged();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

            Bundle parameters = new Bundle();
            parameters.putString("fields", "images");
            request.setParameters(parameters);
            request.executeAsync();

            return null;
        }
    }
}