package com.codexperiments.newsroot.repository.tweet;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.operators.SafeObservableSubscription;
import rx.subjects.Subject;
import rx.util.functions.Func1;
import android.util.Log;

import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.fasterxml.jackson.core.JsonFactory;
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

    // private Observable<String> urlGenerator(final String pUrl,
    // final TimeGap pInitialGap,
    // final int pPageCount,
    // final Observable<TweetPageResponse> pPages)
    // {
    // return Observable.create(new OnSubscribeFunc<String>() {
    // public Subscription onSubscribe(final Observer<? super String> pObserver) {
    // pPages.subscribe(new Observer<TweetPageResponse>() {
    // public void onNext(TweetPageResponse pTweetPageResponse) {
    // TweetPage lTweetPage = pTweetPageResponse.tweetPage();
    // TimeGap lNextGap = lTweetPage.isFull() ? pInitialGap.remainingGap(lTweetPage.timeRange()) : null;
    // String lQuery = TweetQuery.query(mHost, pUrl).withTimeGap(lNextGap).withPageSize(mPageSize).toString();
    // pObserver.onNext(lQuery);
    // }
    //
    // public void onCompleted() {
    // }
    //
    // public void onError(Throwable pThrowable) {
    // }
    // });
    //
    // String lQuery = TweetQuery.query(mHost, pUrl).withTimeGap(pInitialGap).withPageSize(mPageSize).toString();
    // pObserver.onNext(lQuery);
    // return Subscriptions.empty();
    // }
    // });
    // }
    //
    // private Subject<TweetPageResponse, String> urlGenerator(final TimeGap pInitialGap,
    // final int pPageCount,
    // final Subscription pSubscription,
    // final Func1<TweetPageResponse, String> pNextValue)
    // {
    // String initialValue = pNextValue.call(null);
    // final ConcurrentHashMap<Subscription, Observer<? super String>> observers = new ConcurrentHashMap<Subscription, Observer<?
    // super String>>();
    // final AtomicReference<String> currentValue = new AtomicReference<String>(initialValue);
    //
    // OnSubscribeFunc<String> lOnSubscribe = new OnSubscribeFunc<String>() {
    // public Subscription onSubscribe(final Observer<? super String> pObserver) {
    // final SafeObservableSubscription subscription = new SafeObservableSubscription();
    //
    // subscription.wrap(new Subscription() {
    // @Override
    // public void unsubscribe() {
    // // on unsubscribe remove it from the map of outbound observers to notify
    // observers.remove(subscription);
    // if (observers.size() == 0) {
    // pSubscription.unsubscribe();
    // }
    // }
    // });
    //
    // pObserver.onNext(currentValue.get());
    //
    // // on subscribe add it to the map of outbound observers to notify
    // observers.put(subscription, pObserver);
    // return subscription;
    // }
    // };
    //
    // return new Subject<TweetPageResponse, String>(lOnSubscribe) {
    // public void onNext(TweetPageResponse pTweetPageResponse) {
    // String lQuery = pNextValue.call(pTweetPageResponse);
    //
    // if (lQuery != null) {
    // currentValue.set(lQuery);
    // for (Observer<? super String> observer : observers.values()) {
    // observer.onNext(lQuery);
    // }
    // } else {
    // pSubscription.unsubscribe();
    // onCompleted(); // TODO
    // }
    // }
    //
    // public void onCompleted() {
    // // TODO Check if already completed.
    // for (Observer<? super String> observer : observers.values()) {
    // observer.onCompleted();
    // }
    // }
    //
    // public void onError(Throwable pThrowable) {
    // for (Observer<? super String> observer : observers.values()) {
    // observer.onError(pThrowable);
    // }
    // }
    // };
    // }

    private Subject<TweetPageResponse, String> urlGenerator(final TimeGap pInitialGap,
                                                            final int pPageCount,
                                                            final Func1<TweetPageResponse, String> pNextValue)
    {
        final String initialValue = pNextValue.call(null);
        final ConcurrentHashMap<Subscription, Observer<? super String>> observers = new ConcurrentHashMap<Subscription, Observer<? super String>>();
        final AtomicReference<String> currentValue = new AtomicReference<String>(initialValue);

        OnSubscribeFunc<String> lOnSubscribe = new OnSubscribeFunc<String>() {
            public Subscription onSubscribe(final Observer<? super String> pObserver) {
                final SafeObservableSubscription subscription = new SafeObservableSubscription();

                subscription.wrap(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        // on unsubscribe remove it from the map of outbound observers to notify
                        observers.remove(subscription);
                        if (observers.size() == 0) {
                            pSubscription.unsubscribe();
                        }
                    }
                });

                pObserver.onNext(currentValue.get());

                // on subscribe add it to the map of outbound observers to notify
                observers.put(subscription, pObserver);
                return subscription;
            }
        };

        return new Subject<TweetPageResponse, String>(lOnSubscribe) {
            public void onNext(TweetPageResponse pTweetPageResponse) {
                String lQuery = pNextValue.call(pTweetPageResponse);

                if (lQuery != null) {
                    currentValue.set(lQuery);
                    for (Observer<? super String> observer : observers.values()) {
                        observer.onNext(lQuery);
                    }
                } else {
                    pSubscription.unsubscribe();
                    onCompleted(); // TODO
                }
            }

            public void onCompleted() {
                // TODO Check if already completed.
                for (Observer<? super String> observer : observers.values()) {
                    observer.onCompleted();
                }
            }

            public void onError(Throwable pThrowable) {
                for (Observer<? super String> observer : observers.values()) {
                    observer.onError(pThrowable);
                }
            }
        };
    }

    @Override
    public Observable<TweetPageResponse> findTweets(final Timeline pTimeline,
                                                    final TimeGap pTimeGap,
                                                    final int pPageCount,
                                                    final int pPageSize)
    {
        final Func1<TweetPageResponse, String> lNextValue = new Func1<TweetPageResponse, String>() {
            public String call(TweetPageResponse pTweetPageResponse) {
                TimeGap lNextGap = pTimeGap;
                if (pTweetPageResponse != null) {
                    TweetPage lTweetPage = pTweetPageResponse.tweetPage();
                    if (!lTweetPage.isFull()) {
                        return null;
                    } else {
                        lNextGap = pTimeGap.remainingGap(lTweetPage.timeRange());
                    }
                }
                return TweetQuery.query(mHost, TweetQuery.URL_HOME).withTimeGap(lNextGap).withPageSize(mPageSize).toString();
            }
        };

        final Subject<TweetPageResponse, String> lURLs = urlGenerator(pTimeGap, pPageCount, lNextValue);
        final Observable<HttpURLConnection> lConnection = mTweetManager.connect(lURLs);
        final Observable<TweetPageResponse> lResponse = findTweets(lConnection, pPageSize);

        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pTweetPageResponseObserver) {
                final Subscription lInnerSubscription = lResponse.subscribe(new Observer<TweetPageResponse>() {
                    public void onNext(TweetPageResponse pTweetPageResponse) {
                        pTweetPageResponseObserver.onNext(pTweetPageResponse);
                        lURLs.onNext(pTweetPageResponse);
                    }

                    public void onCompleted() {
                        pTweetPageResponseObserver.onCompleted();
                        lURLs.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        pTweetPageResponseObserver.onError(pThrowable);
                        lURLs.onError(pThrowable);
                    }
                });
                // sub.add(lResponse.subscribe(lURLs)); // TODO Merge into one Subscription
                // sub.add(lResponse.subscribe(pTweetPageResponseObserver));
                return new Subscription() {
                    public void unsubscribe() {
                        lInnerSubscription.unsubscribe();
                    }
                };
            }
        });
    }

    private Observable<TweetPageResponse> findTweets(final Observable<HttpURLConnection> pConnections, final int pPageSize) {
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
