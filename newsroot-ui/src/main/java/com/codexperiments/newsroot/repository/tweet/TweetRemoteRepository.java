package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.manager.tweet.TweetQuery.queryFor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import android.util.Log;

import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.manager.tweet.TweetParser;
import com.codexperiments.newsroot.manager.tweet.TweetQuery;
import rx.functions.Func1;

public class TweetRemoteRepository {
    private TweetManager mTweetManager;
    private String mHost;
    private int mPageSize = 20; // TODO
    private TweetParser mParser = new TweetParser();

    public TweetRemoteRepository(TweetManager pTweetManager, String pHost) {
        mTweetManager = pTweetManager;
        mHost = pHost;
    }

    public Timeline findTimeline(String pUsername) {
        return new Timeline(pUsername);
    }

    public Observable<TweetPageResponse> findTweets(final Timeline pTimeline,
                                                    final int pPageSize,
                                                    final Observable<TimeGap> pTimeGaps)
    {
        final Observable<String> lUrls = pTimeGaps.map(new Func1<TimeGap, String>() {
            public String call(TimeGap pTimeGap) {
                return queryFor(mHost, TweetQuery.URL_HOME).withTimeGap(pTimeGap).withPageSize(mPageSize).asURL();
            }
        });
        final Observable<HttpURLConnection> lServerConnections = mTweetManager.connect(lUrls);

        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                return lServerConnections.subscribe(new Observer<HttpURLConnection>() {
                    public void onNext(HttpURLConnection pConnection) {
                        InputStream lInputStream = null;
                        try {
                            lInputStream = new BufferedInputStream(pConnection.getInputStream());
                            // JsonParser lParser = mJSONFactory.createParser(lInputStream);
                            TweetPage lTweetPage = mParser.parseTweetPage(pPageSize, lInputStream);
                            // parseTweetPage(pPageSize, lParser);
                            pObserver.onNext(new TweetPageResponse(lTweetPage, null));
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
                            if (lInputStream != null) try {
                                lInputStream.close();
                            } catch (IOException eIOException) {
                                Log.e(getClass().getSimpleName(), "Problem closing stream", eIOException);
                            }
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
}
