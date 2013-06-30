package com.codexperiments.newsroot.manager.twitter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class TwitterParser
{
    private static final SimpleDateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static List<Tweet> parseTweetList(JsonParser pParser) throws JsonParseException, IOException
    {
        boolean lFinished = false;
        List<Tweet> lResult = new ArrayList<Tweet>(20);

        if (pParser.nextToken() != JsonToken.START_ARRAY) throw new IOException();
        while (!lFinished) {
            switch (pParser.nextToken()) {
            case START_OBJECT:
                lResult.add(parseTweet(pParser));
                break;
            case END_ARRAY:
                lFinished = true;
                break;
            case NOT_AVAILABLE:
                throw new IOException();
            default:
                break;
            }
        }
        return lResult;
    }

    public static Tweet parseTweet(JsonParser pParser) throws JsonParseException, IOException
    {
        boolean lFinished = false;
        String lField = "";
        Tweet lStatus = new Tweet();

        while (!lFinished) {
            switch (pParser.nextToken()) {
            case FIELD_NAME:
                lField = pParser.getCurrentName();
                break;
            case START_OBJECT:
                if ("user".equals(lField)) {
                    parseUser(lStatus, pParser);
                } else {
                    skipObject(pParser);
                }
                break;
            case START_ARRAY:
                skipArray(pParser);
                break;
            case END_OBJECT:
            case NOT_AVAILABLE:
                lFinished = true;
            case END_ARRAY:
                break;
            default:
                if ("id".equals(lField)) {
                    lStatus.setId(pParser.getLongValue());
                } else if ("created_at".equals(lField)) {
                    lStatus.setCreatedAt(getDate(pParser.getText()));
                } else if ("text".equals(lField)) {
                    lStatus.setText(pParser.getText());
                }
                break;
            }
        }
        return lStatus;
    }

    public static Tweet parseUser(Tweet pStatus, JsonParser pParser) throws JsonParseException, IOException
    {
        boolean lFinished = false;
        String lField = "";

        while (!lFinished) {
            switch (pParser.nextToken()) {
            case FIELD_NAME:
                lField = pParser.getCurrentName();
                break;
            case START_OBJECT:
                skipObject(pParser);
                break;
            case START_ARRAY:
                skipArray(pParser);
                break;
            case END_OBJECT:
            case NOT_AVAILABLE:
                lFinished = true;
            case END_ARRAY:
                break;
            default:
                if ("name".equals(lField)) {
                    pStatus.setName(pParser.getText());
                } else if ("screen_name".equals(lField)) {
                    pStatus.setScreenName(pParser.getText());
                }
                break;
            }
        }
        return pStatus;
    }

    public static void skipObject(JsonParser pParser) throws JsonParseException, IOException
    {
        boolean lFinished = false;

        while (!lFinished) {
            switch (pParser.nextToken()) {
            case START_OBJECT:
                skipObject(pParser);
                break;
            case START_ARRAY:
                skipArray(pParser);
                break;
            case END_OBJECT:
            case NOT_AVAILABLE:
                lFinished = true;
            default:
                break;
            }
        }
    }

    public static void skipArray(JsonParser pParser) throws JsonParseException, IOException
    {
        boolean lFinished = false;

        while (!lFinished) {
            switch (pParser.nextToken()) {
            case START_OBJECT:
                skipObject(pParser);
                break;
            case START_ARRAY:
                skipArray(pParser);
                break;
            case END_ARRAY:
            case NOT_AVAILABLE:
                lFinished = true;
            default:
                break;
            }
        }
    }

    private static long getDate(String pDate)
    {
        try {
            return DATE_FORMAT.parse(pDate).getTime();
        } catch (ParseException eParseException) {
            // TODO
            return 0;
        }
    }
}
