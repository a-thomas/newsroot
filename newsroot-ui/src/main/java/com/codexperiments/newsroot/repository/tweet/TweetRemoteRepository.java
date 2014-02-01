package com.codexperiments.newsroot.repository.tweet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import android.util.Log;

import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.manager.tweet.TweetAccessException;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.manager.tweet.TweetManager.QueryHandler;
import com.codexperiments.rx.AndroidScheduler;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class TweetRemoteRepository implements TweetRepository {
    private TweetManager mTweetManager;
    private String mHost;
    private int mPageSize;

    private DateTimeFormatter mDateFormat;

    public TweetRemoteRepository(TweetManager pTweetManager, String pHost) {
        mTweetManager = pTweetManager;
        mHost = pHost;
        mPageSize = 20; // TODO

        mDateFormat = DateTimeFormat.forPattern("EEE MMM d HH:mm:ss z yyyy").withZone(DateTimeZone.UTC);
    }

    @Override
    public Timeline findTimeline(String pUsername) {
        return new Timeline(pUsername);
    }

    @Override
    public Observable<TweetPageResponse> findTweets(Timeline pTimeline, TimeGap pTimeGap, int pPageCount, int pPageSize) {
        return findTweets(TweetQuery.URL_HOME, pTimeGap, pPageCount);
    }

    private Observable<TweetPageResponse> findTweets(final String pUrl, final TimeGap pTimeGap, final int pPageCount) {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                AndroidScheduler.threadPoolForIO().schedule(new Action0() {
                    public void call() {
                        try {
                            TimeGap lTimeGap = pTimeGap;
                            int lPageCount = pPageCount;
                            // XXX
                            // try { Thread.sleep(5000); } catch (InterruptedException eInterruptedException) { }
                            while ((lTimeGap != null) && (lPageCount-- > 0)) {
                                TweetPage lTweetPage = findTweetPage(pUrl, lTimeGap, mPageSize);
                                TweetPageResponse lTweetPageResponse = new TweetPageResponse(lTweetPage, lTimeGap);
                                pObserver.onNext(lTweetPageResponse);
                                lTimeGap = lTweetPageResponse.remainingGap();
                            }
                            pObserver.onCompleted();
                        } catch (TweetAccessException eTweetAccessException) {
                            pObserver.onError(eTweetAccessException);
                        }
                    }
                });
                return Subscriptions.empty();
            }
        });
    }

    private TweetPage findTweetPage(String pUrl, final TimeGap pTimeGap, final int pPageSize) throws TweetAccessException {
        TweetQuery lQuery = TweetQuery.query(mHost, pUrl).withTimeGap(pTimeGap).withPageSize(pPageSize);
        return mTweetManager.query(lQuery, new QueryHandler<TweetPage>() {
            public TweetPage parse(TweetQuery pQuery, JsonParser pParser) throws Exception {
                return parseTweetPage(pTimeGap, pPageSize, pParser);
            }
        });
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
