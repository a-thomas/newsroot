package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.repository.tweet.TweetQuery.queryFor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func2;

import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.rx.Rxt;
import com.codexperiments.rx.Rxt.FeedbackFunc;
import com.codexperiments.rx.Rxt.FeedbackOutput;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class TweetRemoteRepository {
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

    public Timeline findTimeline(String pUsername) {
        return new Timeline(pUsername);
    }

    public Observable<TweetPageResponse> findTweets(final Timeline pTimeline,
                                                    final TimeGap pTimeGap,
                                                    final int pPageCount,
                                                    final int pPageSize)
    {
        FeedbackFunc<String, TweetPageResponse> lMergeFeedback = new FeedbackFunc<String, TweetPageResponse>() {
            public Observable<String> call(Observable<String> pInitialURL, Observable<TweetPageResponse> pTweetPageResponses) {

                return Observable.combineLatest(pInitialURL, pTweetPageResponses, new Func2<String, TweetPageResponse, String>() {
                    public String call(String pInitialURL, TweetPageResponse pTweetPageResponse) {
                        TimeGap lNextGap = pTimeGap;
                        if (pTweetPageResponse != null) {
                            TweetPage lTweetPage = pTweetPageResponse.tweetPage();
                            if (!lTweetPage.isFull()) return null;
                            else lNextGap = pTimeGap.remainingGap(lTweetPage.timeRange());
                        }
                        return queryFor(mHost, TweetQuery.URL_HOME).withTimeGap(lNextGap).withPageSize(mPageSize).asURL();
                    }
                }).takeWhile(Rxt.notNullValue());
            }
        };

        return Rxt.feedback(pPageCount, lMergeFeedback, new FeedbackOutput<String, TweetPageResponse>() {
            public Observable<TweetPageResponse> call(Observable<String> pUrls) {
                return findTweets(mTweetManager.connect(pUrls), pTimeGap, pPageSize);
            }
        });
    }

    private Observable<TweetPageResponse> findTweets(final Observable<HttpURLConnection> pConnections,
                                                     final TimeGap pTimeGap,
                                                     final int pPageSize)
    {
        final JsonFactory mJSONFactory = new JsonFactory();
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                return pConnections.subscribe(new Observer<HttpURLConnection>() {
                    public void onNext(HttpURLConnection pConnection) {
                        InputStream lInputStream = null;
                        try {
                            lInputStream = new BufferedInputStream(pConnection.getInputStream());
                            JsonParser lParser = mJSONFactory.createParser(lInputStream);
                            TweetPage lTweetPage = parseTweetPage(pPageSize, lParser);
                            pObserver.onNext(new TweetPageResponse(lTweetPage, pTimeGap));
                            // is.close();
                        } catch (IOException eIOException) {
                            // try {
                            // respCode = ((HttpURLConnection) conn).getResponseCode();
                            // es = ((HttpURLConnection) conn).getErrorStream();
                            // int ret = 0;
                            // // read the response body
                            // while ((ret = es.read(buf)) > 0) {
                            // processBuf(buf);
                            // }
                            // // close the errorstream
                            // es.close();
                            // } catch (IOException ex) {
                            // // deal with the exception
                            // }
                            pObserver.onError(eIOException);
                        } finally {
                            pConnection.disconnect();
                        }
                    }

                    public void onCompleted() {
                        pObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        pObserver.onError(pThrowable);
                    }
                });
            }
        });
    }

    private TweetPage parseTweetPage(int pPageSize, JsonParser pParser) throws JsonParseException, IOException {
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
