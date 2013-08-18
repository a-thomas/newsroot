package com.codexperiments.newsroot.repository.twitter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import rx.util.BufferClosing;
import rx.util.BufferClosings;
import rx.util.functions.Func1;
import android.util.Pair;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.manager.twitter.TwitterManager.QueryHandler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class TwitterAPI {
    private static final int SAFETY_COUNTER = 10;

    private TwitterManager mTwitterManager;
    private String mHost;
    private SimpleDateFormat mDateFormat;

    public TwitterAPI(TwitterManager pTwitterManager, String pHost) {
        mTwitterManager = pTwitterManager;
        mHost = pHost;
        mDateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.US);
        mDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Pair<Observable<Tweet>, Observable<BufferClosing>> getHome(final TimeGap pTimeGap, final int pPageSize) {
        final PublishSubject<BufferClosing> lController = PublishSubject.create();
        final Observable<Tweet> lTweets = Observable.create(new Func1<Observer<Tweet>, Subscription>() {
            public Subscription call(final Observer<Tweet> pObserver) {
                try {
                    TimeGap lRemainingGap = pTimeGap;
                    int lSafetyCounter = 0;
                    // Retrieve a new page of data until we receive an empty or partial page (which means there is no more data).
                    // The safety counter is just here to avoid a possible infinite loop. TODO Should find a better way.
                    while ((lRemainingGap != null) && (++lSafetyCounter < SAFETY_COUNTER)) {
                        TwitterQuery lQuery = TwitterQuery.queryHome(mHost).withTimeGap(lRemainingGap).withPageSize(pPageSize);

                        lRemainingGap = mTwitterManager.query(lQuery, new QueryHandler<TimeGap>() {
                            public TimeGap parse(TwitterQuery pQuery, JsonParser pParser) throws Exception {
                                return parseTweets(pQuery, pObserver, pParser);
                            }
                        });
                        lController.onNext(BufferClosings.create());
                    }

                    pObserver.onCompleted();
                    lController.onCompleted();
                } catch (TwitterAccessException eTwitterAccessException) {
                    pObserver.onError(eTwitterAccessException);
                    lController.onError(eTwitterAccessException);
                }
                return Subscriptions.empty();
            }
        });
        return new Pair<Observable<Tweet>, Observable<BufferClosing>>(lTweets, lController);
    }

    private TimeGap parseTweets(TwitterQuery pQuery, Observer<Tweet> pObserver, JsonParser pParser)
        throws JsonParseException, IOException
    {
        if (pParser.nextToken() != JsonToken.START_ARRAY) throw new IOException();

        int lTweetCount = 0;
        long lEarliestBound = 0;
        boolean lFinished = false;
        while (!lFinished) {
            switch (pParser.nextToken()) {
            case START_OBJECT:
                ++lTweetCount;
                Tweet lTweet = parseTweet(pParser);
                lEarliestBound = lTweet.getTimelineId();
                pObserver.onNext(lTweet);
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

        if ((lTweetCount > 0) && (lTweetCount >= pQuery.getPageSize())) {
            return new TimeGap(lEarliestBound, pQuery.getTimeGap().getOldestBound());
        } else {
            return null;
        }
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
