/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.giada.stickypoliciesapp.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    final static String MY_BASE_URL ="10.0.2.2:8080";
    final static String MY_SERVER_DOMAIN = "PolicyServer";
    private final static String TAG = NetworkUtils.class.getSimpleName();

    public static URL buildUrl(String targetUri, String queryKeyParam, String queryValueParam) {
        Uri.Builder builder = new Uri.Builder();
        if (queryKeyParam.isEmpty()) {
            builder.scheme("http")
                    .encodedAuthority(MY_BASE_URL)
                    .appendPath(MY_SERVER_DOMAIN)
                    .appendPath(targetUri);
        } else {
            builder.scheme("http")
                    .encodedAuthority(MY_BASE_URL)
                    .appendPath(MY_SERVER_DOMAIN)
                    .appendPath(targetUri)
                    .appendQueryParameter(queryKeyParam, queryValueParam);
        }
        Uri builtUri = builder.build();

        URL url = null;
        try {
            url = new URL((builtUri.toString()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResponseFromHttpUrl(URL url, String requestMethod, String postData, String contentType) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            Log.d(TAG, "Opened connection with url " + url.toString());
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setChunkedStreamingMode(0);

            if (!postData.isEmpty()) {
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", contentType);
                try {
                    OutputStream out = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(out, "UTF-8"));
                    writer.write(postData);
                    writer.flush();
                    writer.close();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            urlConnection.connect();
            Log.d(TAG, "Message sent!");

            Log.d(TAG, "Response code: " + urlConnection.getResponseCode());
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                Log.d(TAG, "Found something in response body");
                return scanner.next();
            } else {
                Log.d(TAG, "Response body is empty");
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}