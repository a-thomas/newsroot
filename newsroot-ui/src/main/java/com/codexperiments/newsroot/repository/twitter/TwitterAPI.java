package com.codexperiments.newsroot.repository.twitter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;
import rx.util.BufferClosing;
import rx.util.BufferClosings;
import rx.util.functions.Action1;
import rx.util.functions.Func0;
import rx.util.functions.Func1;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.Database;
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

    // public Observable<Tweet> getHome(final TimeGap pTimeGap, final int pPageSize) {
    // TwitterQuery lQuery = TwitterQuery.queryHome(mHost)
    // .withParam("count", pPageSize)
    // .withParamIf(!pTimeGap.isFutureGap(), "max_id", pTimeGap.getEarliestBound() - 1)
    // .withParamIf(!pTimeGap.isPastGap(), "since_id", pTimeGap.getOldestBound());
    // return query(lQuery, new TwitterQuery.Handler<Observable<Tweet>>() {
    // public Observable<Tweet> parse(JsonParser pParser) throws Exception {
    // return parseTweets(pParser);
    // }
    // });
    // }
    // public Observable<Observable<Tweet>> getHome(final TimeGap pTimeGap, final int pPageSize) {
    // Subject<Observable<Tweet>, Observable<Tweet>> lSubject;
    // TwitterQuery lQuery = TwitterQuery.queryHome(mHost)
    // .withParam("count", pPageSize)
    // .withParamIf(!pTimeGap.isFutureGap(), "max_id", pTimeGap.getEarliestBound() - 1)
    // .withParamIf(!pTimeGap.isPastGap(), "since_id", pTimeGap.getOldestBound());
    // Observable<Tweet> lTweets = query(lQuery, new TwitterQuery.Handler<Observable<Tweet>>() {
    // public Observable<Tweet> parse(JsonParser pParser) throws Exception {
    // return parseTweets(pParser);
    // }
    // });
    // lSubject.onNext(lTweets);
    //
    // Observable.publish(Observable.create(new Func1<Observer<Observable<Tweet>>, Subscription>() {
    // public Subscription call(Observer<Observable<Tweet>> pObserver) {
    // TwitterQuery lQuery = TwitterQuery.queryHome(mHost)
    // .withParam("count", pPageSize)
    // .withParamIf(!pTimeGap.isFutureGap(), "max_id", pTimeGap.getEarliestBound() - 1)
    // .withParamIf(!pTimeGap.isPastGap(), "since_id", pTimeGap.getOldestBound());
    // Observable<Tweet> lTweets = query(lQuery, new TwitterQuery.Handler<Observable<Tweet>>() {
    // public Observable<Tweet> parse(JsonParser pParser) throws Exception {
    // return parseTweets(pParser);
    // }
    // });
    // pObserver.onNext(lTweets);
    // return Subscriptions.empty();
    // }
    // }));
    //
    // TwitterQuery lQuery = TwitterQuery.queryHome(mHost)
    // .withParam("count", pPageSize)
    // .withParamIf(!pTimeGap.isFutureGap(), "max_id", pTimeGap.getEarliestBound() - 1)
    // .withParamIf(!pTimeGap.isPastGap(), "since_id", pTimeGap.getOldestBound());
    // return query(lQuery, new TwitterQuery.Handler<Observable<Tweet>>() {
    // public Observable<Tweet> parse(JsonParser pParser) throws Exception {
    // return parseTweets(pParser);
    // }
    // });
    // }
    //
    // public Observable<Tweet> getHome3(final TimeGap pTimeGap, final int pPageSize) {
    // TwitterQuery lQuery = TwitterQuery.queryHome(mHost)
    // .withParam("count", pPageSize)
    // .withParamIf(!pTimeGap.isFutureGap(), "max_id", pTimeGap.getEarliestBound() - 1)
    // .withParamIf(!pTimeGap.isPastGap(), "since_id", pTimeGap.getOldestBound());
    // return query(lQuery, new TwitterQuery.Handler<Observable<Tweet>>() {
    // public Observable<Tweet> parse(JsonParser pParser) throws Exception {
    // return parseTweets(pParser);
    // }
    // });
    // }
    //
    // private <TResult> Observable<TResult> query(final TwitterQuery pQuery,
    // final TwitterQuery.Handler<Observable<TResult>> pHandler)
    // {
    // return Observable.create(new Func1<Observer<TResult>, Subscription>() {
    // public Subscription call(Observer<TResult> pObserver) {
    // try {
    // mTwitterManager.query(pQuery, pHandler);
    // pObserver.onCompleted();
    // } catch (TwitterAccessException eTwitterAccessException) {
    // pObserver.onError(eTwitterAccessException);
    // }
    // return Subscriptions.empty();
    // }
    // });
    // }
    //
    // public Observable<Tweet> getHome5(final TimeGap pTimeGap, final int pPageSize) {
    // Func0<Observable<BufferClosing>> close = new Func0<Observable<BufferClosing>>() {
    // public Observable<BufferClosing> call() {
    // return Observable.create(new Func1<Observer<BufferClosing>, Subscription>() {
    // public Subscription call(Observer<BufferClosing> observer) {
    // observer.onNext(BufferClosings.create());
    // observer.onCompleted();
    // return Subscriptions.empty();
    // }
    // });
    // }
    // };
    //
    // return Observable.create(new Func1<Observer<Tweet>, Subscription>() {
    // public Subscription call(final Observer<Tweet> pObserver) {
    // try {
    // TimeGap lRemainingGap = pTimeGap;
    // while (lRemainingGap != null) {
    // TwitterQuery lQuery = TwitterQuery.queryHome(mHost).withTimeGap(pTimeGap).withPageSize(pPageSize);
    //
    // lRemainingGap = mTwitterManager.query(lQuery, new TwitterQuery.Handler<TimeGap>() {
    // public TimeGap parse(TwitterQuery pQuery, JsonParser pParser) throws Exception {
    // return parseTweets(pQuery, pObserver, pParser);
    // }
    // });
    // }
    // pObserver.onCompleted();
    // } catch (TwitterAccessException eTwitterAccessException) {
    // pObserver.onError(eTwitterAccessException);
    // }
    // return Subscriptions.empty();
    // }
    // });
    // }

    public interface ControlledSubscription extends Subscription {
        Observer<BufferClosing> getController();
    }

    public Pair<Observable<Tweet>, Observable<BufferClosing>> getHome(final TimeGap pTimeGap, final int pPageSize) {
        final PublishSubject<BufferClosing> lController = PublishSubject.create();
        final Observable<Tweet> lTweets = Observable.create(new Func1<Observer<Tweet>, Subscription>() {
            public Subscription call(final Observer<Tweet> pObserver) {
                try {
                    TimeGap lRemainingGap = pTimeGap;
                    while (lRemainingGap != null) {
                        TwitterQuery lQuery = TwitterQuery.queryHome(mHost).withTimeGap(pTimeGap).withPageSize(pPageSize);

                        lRemainingGap = mTwitterManager.query(lQuery, new TwitterQuery.Handler<TimeGap>() {
                            public TimeGap parse(TwitterQuery pQuery, JsonParser pParser) throws Exception {
                                return parseTweets(pQuery, pObserver, pParser);
                            }
                        });
                        lController.onNext(BufferClosings.create());
                    }
                    lController.onCompleted();
                    pObserver.onCompleted();
                } catch (TwitterAccessException eTwitterAccessException) {
                    lController.onError(eTwitterAccessException);
                    pObserver.onError(eTwitterAccessException);
                }
                return Subscriptions.empty();
            }
        });
        // .publish();
        return new Pair<Observable<Tweet>, Observable<BufferClosing>>(lTweets, lController);
    }

    public <T> Observable<T> commitTweets(final Observable<T> pObservable,
                                          final Observable<BufferClosing> pCloser,
                                          final Database pDatabase,
                                          final Action1<T> pAction,
                                          final Action1<List<T>> pBatchAction)
    {
        final Func0<Observable<BufferClosing>> closer = new Func0<Observable<BufferClosing>>() {
            public Observable<BufferClosing> call() {
                return pCloser;
            }
        };

        return Observable.create(new Func1<Observer<T>, Subscription>() {
            public Subscription call(final Observer<T> pObserver) {
                return pObservable.buffer(closer).subscribe(new Observer<List<T>>() {
                    private Database mDatabase = pDatabase;
                    private SQLiteDatabase mConnection = null;
                    private List<T> mValues;

                    public void onNext(List<T> pValues) {
                        mValues = pValues;
                        if (mConnection == null) {
                            mConnection = mDatabase.getWritableDatabase();
                        }

                        mConnection.beginTransaction();
                        try {
                            // let the exception be thrown if func fails as a SafeObserver wrapping this will handle it
                            for (T lValue : pValues) {
                                pAction.call(lValue);
                            }
                            pBatchAction.call(pValues);
                            mConnection.setTransactionSuccessful();
                            mConnection.endTransaction();

                            for (T value : mValues) {
                                pObserver.onNext(value);
                            }
                        } catch (Exception eException) {
                            pObserver.onError(eException);
                            mConnection.endTransaction();
                        }
                    }

                    public void onCompleted() {
                        pObserver.onCompleted();
                    }

                    public void onError(Throwable pException) {
                        if (mConnection != null) mConnection.endTransaction();
                        pObserver.onError(pException);
                    }
                });
            }
        });
    }

    public <T, R> Observable<R> downcast(Observable<T> pObservable) {
        return pObservable.map(new Func1<T, R>() {
            @SuppressWarnings("unchecked")
            public R call(T pT) {
                return (R) pT;
            }
        });
    }

    public <T, R> Observable<R> downcast(Observable<T> pObservable, final Class<R> pDowncastedType) {
        return pObservable.map(new Func1<T, R>() {
            public R call(T pT) {
                return pDowncastedType.cast(pT);
            }
        });
    }

    private TimeGap parseTweets(TwitterQuery pQuery, Observer<Tweet> pObserver, final JsonParser pParser)
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

        if ((lTweetCount > 0) && (lTweetCount == pQuery.getPageSize())) {
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
