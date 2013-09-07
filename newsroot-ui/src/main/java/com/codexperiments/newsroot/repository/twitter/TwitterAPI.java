package com.codexperiments.newsroot.repository.twitter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Func1;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.manager.twitter.TwitterManager.QueryHandler;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
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

    public Observable<TweetPage> findHomeTweets(TimeGap pTimeGap, int pPageCount, int pPageSize) {
        return findTweets(TwitterQuery.URL_HOME, pTimeGap, pPageSize, pPageCount);
    }

    private Observable<TweetPage> findTweets(final String pUrl, final TimeGap pTimeGap, final int pPageSize, final int pPageCount)
    {
        return Observable.create(new Func1<Observer<TweetPage>, Subscription>() {
            public Subscription call(final Observer<TweetPage> pObserver) {
                AndroidScheduler.threadPoolForIO().schedule(new Action0() {
                    public void call() {
                        try {
                            TimeGap lRemainingTimeGap = pTimeGap;
                            int lPageCount = pPageCount;
                            while ((lRemainingTimeGap != null) && (lPageCount-- > 0)) {
                                TweetPage lTweetPage = findTweetPage(pUrl, pTimeGap, pPageSize);
                                pObserver.onNext(lTweetPage);
                                lRemainingTimeGap = lTweetPage.remainingGap();
                            }
                        } catch (TwitterAccessException eTwitterAccessException) {
                            pObserver.onError(eTwitterAccessException);
                        }
                    }
                });
                return Subscriptions.empty();
            }
        });
    }

    private TweetPage findTweetPage(String pUrl, final TimeGap pTimeGap, final int pPageSize) throws TwitterAccessException {
        TwitterQuery lQuery = TwitterQuery.query(mHost, pUrl).withTimeGap(pTimeGap).withPageSize(pPageSize);
        return mTwitterManager.query(lQuery, new QueryHandler<TweetPage>() {
            public TweetPage parse(TwitterQuery pQuery, JsonParser pParser) throws Exception {
                return parseTweetPage(pTimeGap, pPageSize, pParser);
            }
        });
    }

    private TweetPage parseTweetPage(TimeGap pTimeGap, int pPageSize, JsonParser pParser) throws JsonParseException, IOException {
        if (pParser.nextToken() != JsonToken.START_ARRAY) throw new IOException();
        List<Tweet> lTweets = new ArrayList<Tweet>(pPageSize);

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
        return new TweetPage(lTweets, pTimeGap, pPageSize);
    }

    private Tweet parseTweet(JsonParser pParser) throws JsonParseException, IOException {
        boolean lFinished = false;
        String lField = "";
        Tweet lTweet = new Tweet();

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
        return lTweet;
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
