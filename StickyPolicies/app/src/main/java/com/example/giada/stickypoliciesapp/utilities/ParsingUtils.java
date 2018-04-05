package com.example.giada.stickypoliciesapp.utilities;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Giada on 23/03/2018.
 */

public class ParsingUtils {
    public final static String DATA_TYPE_PICTURE = "picture";
    public final static String DATA_TYPE_TEXT = "text";
    public final static String DATA_TYPE_VIDEO = "video";
    public final static String DATA_TYPE_AUDIO = "audio";
    public final static String DATA_TYPE_POSITION = "position";

    public final static String CERT_SN_TAG = "certificateSerialNumber";
    public final static String DATA_TYPE_TAG = "dataType";


    public static String correctPolicyFile(String original, String xmlTag, String newContent) {
        ////////////////////////////////////////
        ///////////// ---- HARDCODED -- NOT GOOD
        String[] splitDocument = original.split(xmlTag);
        String modifiedDocument = new String();
        for (int i = 0; i < splitDocument.length; i++) {
            if (i % 2 == 0) {
                modifiedDocument = modifiedDocument.concat(splitDocument[i]);
            } else {
                modifiedDocument = modifiedDocument.concat(xmlTag + ">" + newContent + "</" + xmlTag);
            }
        }
        ///////////// ---- HARDCODED -- NOT GOOD
        ////////////////////////////////////////

        return modifiedDocument;
    }

    public static String getTagContent(String text, String targetXmlTag) {
        ////////////////////////////////////////
        ///////////// ---- HARDCODED -- NOT GOOD
        String[] splitDocument = text.split(targetXmlTag);
        String result = splitDocument[1].replaceAll("(<|>|/)", "");
        ///////////// ---- HARDCODED -- NOT GOOD
        ////////////////////////////////////////
        return result;
    }

    public static String writeInternalStorage(Context appContext, String filename, String content) {
        FileOutputStream outputStream;
        try {
            outputStream = appContext.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File directory = appContext.getFilesDir();
        File file = new File(directory, filename);
        return file.getAbsolutePath();
    }

    public static String readInternalStorage(Context applicationContext, String absolutePath) {
        File policyFile = new File(absolutePath);
        FileInputStream fis = null;
        try {
            fis = applicationContext.openFileInput(policyFile.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
