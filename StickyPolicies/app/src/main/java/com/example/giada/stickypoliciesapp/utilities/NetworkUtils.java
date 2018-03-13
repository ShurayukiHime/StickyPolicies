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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

    final static String MY_BASE_URL_PORT =
            //"192.168.1.8:8080";
            "10.0.2.2:8080";

    final static String MY_SERVER_DOMAIN = "PolicyServer";
    private static String TAG = "NetworkUtils";

    public static URL buildUrl(String targetUri, String queryKeyParam, String queryValueParam) {
        /*Uri builtUri = Uri.parse(MY_BASE_URL_PORT + "/" + MY_SERVER_DOMAIN + "/" + OBTAIN_CERT_PATH).buildUpon()
                .appendQueryParameter(PARAM_QUERY, searchQuery)
                .appendQueryParameter(PARAM_SORT, sortBy)
                .build();*/
        Uri.Builder builder = new Uri.Builder();
        if (queryKeyParam.isEmpty()) {
            builder.scheme("http")
                    .encodedAuthority(MY_BASE_URL_PORT)
                    .appendPath(MY_SERVER_DOMAIN)
                    .appendPath(targetUri);
        } else {
            builder.scheme("http")
                    .encodedAuthority(MY_BASE_URL_PORT)
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

    public static String getResponseFromHttpUrl(URL url, String requestMethod, String postData) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            Log.d("NetworkUtils", "Opened connection with url " + url.toString());
            urlConnection.setRequestMethod(requestMethod);
            urlConnection.setChunkedStreamingMode(0);

            if (!postData.isEmpty()) {
                urlConnection.setDoOutput(true);
                OutputStream out = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(out, "UTF-8"));
                writer.write("PostData=" + postData);
                writer.flush();
                writer.close();
                out.close();
            }
            urlConnection.connect();
            Log.d("NetworkUtils", "Message sent!");

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
                Log.d(TAG, "Response code: 200!");
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                Log.d("NetworkUtils", "Found something in response!");
                return scanner.next();
            } else {
                Log.d("NetworkUtils", "Tough luck!");
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static String extractBody (String htmlDocument) {
        String body = null;
        Document doc = Jsoup.parse(htmlDocument);
        body = doc.body().text();
        return body;
    }
}