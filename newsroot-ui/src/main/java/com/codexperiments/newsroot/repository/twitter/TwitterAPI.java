package com.codexperiments.newsroot.repository.twitter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func1;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class TwitterAPI {
    private TwitterManager mTwitterManager;
    private String mHost;
    private SimpleDateFormat mDateFormat;

    public TwitterAPI(TwitterManager pTwitterManager, String pHost) {
        mTwitterManager = pTwitterManager;
        mHost = pHost;
        mDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Observable<Tweet> getHome(TimeGap pTimeGap, int pPageSize) throws TwitterAccessException {
        TwitterQuery lQuery = TwitterQuery.queryHome(mHost)
                                          .withParam("count", pPageSize)
                                          .withParamIf(!pTimeGap.isFutureGap(), "max_id", pTimeGap.getEarliestBound() - 1)
                                          .withParamIf(!pTimeGap.isPastGap(), "since_id", pTimeGap.getOldestBound());

        return mTwitterManager.query(lQuery, new TwitterQuery.Handler<Observable<Tweet>>() {
            public Observable<Tweet> parse(JsonParser pParser) throws Exception {
                return parseTweets(pParser);
            }
        });
    }

    public Observable<Tweet> parseTweets(final JsonParser pParser) throws JsonParseException, IOException {
        return Observable.create(new Func1<Observer<Tweet>, Subscription>() {
            public Subscription call(Observer<Tweet> pObserver) {
                try {
                    boolean lFinished = false;
                    if (pParser.nextToken() != JsonToken.START_ARRAY) throw new IOException();

                    while (!lFinished) {
                        switch (pParser.nextToken()) {
                        case START_OBJECT:
                            pObserver.onNext(parseTweet(pParser));
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
                    pObserver.onCompleted();
                } catch (JsonParseException eJsonParseException) {
                    pObserver.onError(eJsonParseException);
                } catch (IOException eIOException) {
                    pObserver.onError(eIOException);
                }
                return Subscriptions.empty();
            }
        });
    }

    private Tweet parseTweet(JsonParser pParser) throws JsonParseException, IOException {
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

    private Tweet parseUser(Tweet pStatus, JsonParser pParser) throws JsonParseException, IOException {
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
            return mDateFormat.parse(pDate).getTime();
        } catch (ParseException eParseException) {
            // TODO
            return 0;
        }
    }
}
