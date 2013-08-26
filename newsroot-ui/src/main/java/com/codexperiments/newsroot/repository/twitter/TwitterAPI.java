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
import com.codexperiments.newsroot.manager.twitter.TwitterManager.QueryHandler;
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

    public Observable<Observable<Tweet>> getHome(final TimeGap pTimeGap, final int pPageSize) {
        return Observable.create(new Func1<Observer<Observable<Tweet>>, Subscription>() {
            public Subscription call(final Observer<Observable<Tweet>> pObserver) {
                pObserver.onNext(getTweets(pObserver, pTimeGap, pPageSize));
                return Subscriptions.empty();
            }
        });
    }

    public Observable<Tweet> getTweets(final Observer<Observable<Tweet>> pPagingObserver,
                                       final TimeGap pTimeGap,
                                       final int pPageSize)
    {
        return Observable.create(new Func1<Observer<Tweet>, Subscription>() {
            public Subscription call(final Observer<Tweet> pObserver) {
                TimeGap lRemainingGap = null;
                try {
                    TwitterQuery lQuery = TwitterQuery.queryHome(mHost).withTimeGap(pTimeGap).withPageSize(pPageSize);
                    lRemainingGap = mTwitterManager.query(lQuery, new QueryHandler<TimeGap>() {
                        public TimeGap parse(TwitterQuery pQuery, JsonParser pParser) throws Exception {
                            return parseTweets(pObserver, pTimeGap, pPageSize, pParser);
                        }
                    });
                } catch (TwitterAccessException eTwitterAccessException) {
                    pObserver.onError(eTwitterAccessException);
                }

                // Retrieve next page.
                if (lRemainingGap != null) {
                    pPagingObserver.onNext(getTweets(pPagingObserver, pTimeGap, pPageSize));
                }
                return Subscriptions.empty();
            }
        }).cache();
    }

    private TimeGap parseTweets(Observer<Tweet> pObserver, TimeGap pTimeGap, int pPageSize, JsonParser pParser)
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

        if ((lTweetCount > 0) && (lTweetCount >= pPageSize)) {
            return new TimeGap(lEarliestBound, pTimeGap.getOldestBound());
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
