package com.codexperiments.newsroot.manager.tweet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.util.Log;

import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class TweetParser {
    private JsonFactory mJSONFactory;
    private DateTimeFormatter mDateFormat;

    public interface ParserHandler<TResult> {
        TResult parse(JsonParser pParser) throws Exception;
    }

    public TweetParser(Context pContext) {
        super();
        mJSONFactory = new JsonFactory();
        mDateFormat = DateTimeFormat.forPattern("EEE MMM d HH:mm:ss z yyyy").withZone(DateTimeZone.UTC);
    }

    public TweetPage parseTweetPage(final TimeGap pTimeGap, final int pPageSize, InputStream lInputStream)
        throws TweetAccessException
    {
        JsonParser lParser = null;
        try {
            lParser = mJSONFactory.createParser(lInputStream);
            ParserHandler<TweetPage> lParserHandler = new ParserHandler<TweetPage>() {
                public TweetPage parse(JsonParser pParser) throws Exception {
                    return parseTweetPage(pTimeGap, pPageSize, pParser);
                }
            };
            return lParserHandler.parse(lParser);
        } catch (MalformedURLException eMalformedURLException) {
            throw TweetAccessException.from(eMalformedURLException);
        } catch (IOException eIOException) {
            throw TweetAccessException.from(eIOException);
        } catch (OAuthMessageSignerException eOAuthMessageSignerException) {
            throw TweetAccessException.from(eOAuthMessageSignerException);
        } catch (OAuthExpectationFailedException eOAuthExpectationFailedException) {
            throw TweetAccessException.from(eOAuthExpectationFailedException);
        } catch (OAuthCommunicationException eOAuthCommunicationException) {
            throw TweetAccessException.from(eOAuthCommunicationException);
        } catch (Exception eException) {
            throw TweetAccessException.from(eException);
        } finally {
            try {
                if (lParser != null) lParser.close();
            } catch (IOException eIOException) {
                eIOException.printStackTrace();
            }
            try {
                if (lInputStream != null) lInputStream.close();
            } catch (IOException eIOException) {
                eIOException.printStackTrace();
            }
        }
    }

    private TweetPage parseTweetPage(TimeGap pTimeGap, int pPageSize, JsonParser pParser) throws JsonParseException, IOException {
        if (pParser.nextToken() != JsonToken.START_ARRAY) throw new IOException();
        List<TweetDTO> lTweets = new ArrayList<TweetDTO>(pPageSize);

        boolean lFinished = false;
        while (!lFinished) {
            switch (pParser.nextToken()) {
            case START_OBJECT:
                lTweets.add(parseTweet(pParser));
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
        return new TweetPage(lTweets.toArray(new TweetDTO[lTweets.size()]), pPageSize);
    }

    private TweetDTO parseTweet(JsonParser pParser) throws JsonParseException, IOException {
        boolean lFinished = false;
        String lField = "";
        TweetDTO lTweet = new TweetDTO();

        while (!lFinished) {
            switch (pParser.nextToken()) {
            case FIELD_NAME:
                lField = pParser.getCurrentName();
                break;
            case START_OBJECT:
                if ("user".equals(lField)) {
                    parseUser(lTweet, pParser);
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
                    lTweet.setId(pParser.getLongValue());
                } else if ("created_at".equals(lField)) {
                    lTweet.setCreatedAt(getDate(pParser.getText()));
                } else if ("text".equals(lField)) {
                    lTweet.setText(pParser.getText());
                }
                break;
            }
        }
        Log.e(TweetManager.class.getSimpleName(), lTweet.toString());
        return lTweet;
    }

    private TweetDTO parseUser(TweetDTO pStatus, JsonParser pParser) throws JsonParseException, IOException {
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

    private void skipObject(JsonParser pParser) throws JsonParseException, IOException {
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

    private void skipArray(JsonParser pParser) throws JsonParseException, IOException {
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

    private long getDate(String pDate) {
        try {
            return mDateFormat.parseDateTime(pDate).getMillis();
        } catch (IllegalArgumentException eIllegalArgumentException) {
            // TODO
            return 0;
        }
    }
}
