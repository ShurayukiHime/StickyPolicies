package com.example.giada.stickypoliciesapp.utilities;

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


    public static String correctPolicyFile (String original, String xmlTag, String newContent) {
        ////////////////////////////////////////
        ///////////// ---- HARDCODED -- NOT GOOD
        String[] splitDocument = original.split(xmlTag);
        String modifiedDocument = splitDocument[0] + xmlTag + ">" + newContent + "</" + xmlTag + splitDocument[2];
        ///////////// ---- HARDCODED -- NOT GOOD
        ////////////////////////////////////////

        return modifiedDocument;
    }

    public static String getTagContent (String text, String targetXmlTag) {
        ////////////////////////////////////////
        ///////////// ---- HARDCODED -- NOT GOOD
        String[] splitDocument = text.split(targetXmlTag);
        String result = splitDocument[1].replaceAll("(<|>|/)", "");
        ///////////// ---- HARDCODED -- NOT GOOD
        ////////////////////////////////////////
        return result;
    }
}
