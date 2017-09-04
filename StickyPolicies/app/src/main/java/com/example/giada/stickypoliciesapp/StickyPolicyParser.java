package com.example.giada.stickypoliciesapp;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giada on 02/09/2017.
 */

public class StickyPolicyParser {
    private static final String ns = null;

    public List<Entry> parse(InputStream inputStream) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false); //don't process namespaces
            parser.setInput(inputStream, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            inputStream.close();
        }
    }

    private List<Entry> readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {
        List<Entry> entries = new ArrayList<Entry>();
        String enclosingTag = "data package";

        parser.require(XmlPullParser.START_TAG, ns, enclosingTag); //prendi lo Start Tag del livello "feed"
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            String tag = parser.getName(); //ho incontrato un altro tag e devo cercare quello sotto in gerarchia
            if (tag.equals("entry"))
                entries.add(readEntry(parser));
            else
                skip(parser);
        }
        return entries;
    }

    private Entry readEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "entry"); //cominciamo a leggere una entry
        String title = null;
        String link = null;
        String summary = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG)
                continue;
            String tag = parser.getName();
            if (tag.equals("title")) {
                title = readTitle(parser);
            } else if (tag.equals("summary")) {
                summary = readSummary(parser);
            } else if (tag.equals("link")) {
                link = readLink(parser);
            } else {
                skip(parser);
            }
        }
        return new Entry(title, link, summary);
    }

    private void skip(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String readSummary(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "summary");
        String title = readText(parser);
        parser.require(XmlPullParser.START_TAG, ns, "summary");
        return title;
    }

    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "link");
        String title = readText(parser);
        parser.require(XmlPullParser.START_TAG, ns, "link");
        return title;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.START_TAG, ns, "title");
        return title;
    }

    public class Entry {
        public final String title;
        public final String link;
        public final String summary;

        private Entry(String title, String link, String summary) {
            this.title = title;
            this.link = link;
            this.summary = summary;
        }

        @Override
        public String toString() {
            String result = "title: " + this.title + "; link: " + this.link + "; summary" + this.summary + ".\n";
            return result;
        }
    }
}
