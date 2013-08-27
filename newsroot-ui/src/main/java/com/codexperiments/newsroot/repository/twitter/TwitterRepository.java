package com.codexperiments.newsroot.repository.twitter;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.app.Application;
import android.database.Cursor;

import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.Query;
import com.codexperiments.newsroot.manager.twitter.ResultHandler;
import com.codexperiments.newsroot.manager.twitter.TimeGapDAO;
import com.codexperiments.newsroot.manager.twitter.TweetDAO;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.COL_VIEW_TIMELINE;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.DB_TWITTER;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.manager.twitter.ViewTimelineDAO;
import com.codexperiments.robolabor.task.TaskManager;

public class TwitterRepository {
    private static final int DEFAULT_PAGE_COUNT = 5;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private EventBus mEventBus;
    private TwitterAPI mTwitterAPI;

    private TwitterDatabase mDatabase;
    private TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;
    private ViewTimelineDAO mViewTimelineDAO;

    public TwitterRepository(Application pApplication,
                             EventBus pEventBus,
                             TaskManager pTaskManager,
                             TwitterManager pTwitterManager,
                             TwitterAPI pTwitterAPI,
                             TwitterDatabase pDatabase)
    {
        super();
        mEventBus = pEventBus;
        mEventBus.registerListener(this);
        mTwitterAPI = pTwitterAPI;

        mDatabase = pDatabase;
        mTweetDAO = new TweetDAO(mDatabase);
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        mViewTimelineDAO = new ViewTimelineDAO(mDatabase);
    }

    public Observable<Observable<News>> findLatestNews(Timeline pTimeline) {
        // List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        // pTimeline.appendOldItems(lResult);
        // lResult = findTweetsInGap(new TimeGap(-1, pTimeline.getEarliestBound()));
        // pTimeline.appendNewItems(lResult);
        // return lResult;
        // Observable<News> lTweetsFromCache = findTweetsFromCache(new TimeGap());
        return findTweetsFromServer(new TimeGap(-1, -1), DEFAULT_PAGE_COUNT, DEFAULT_PAGE_SIZE);
        // return Pair.create(Observable.concat(lTweetsFromNetwork.first, lTweetsFromCache),
        // lTweetsFromNetwork.second);
        // return Pair.create(Observable.concat(lTweetsFromCache, lTweetsFromNetwork.first),
        // lTweetsFromNetwork.second);
    }

    public Observable<Observable<News>> findOlderNews(final Timeline pTimeline) {
        // List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        // pTimeline.appendOldItems(lResult);
        // return lResult;
        Observable.create(new Func1<Observer<Observable<News>>, Subscription>() {
            public Subscription call(Observer<Observable<News>> pObserver) {
                if (pTimeline.toString() != null) {
                    return findTweetsFromRepository(new TimeGap(/* pTimeline.getOldestBound() */-1, -1)).subscribe(pObserver);
                } else {
                    return findTweetsFromServer(new TimeGap(-1, -1), 1, DEFAULT_PAGE_SIZE).subscribe(pObserver);
                }
            }});
        if (pTimeline.toString() != null) {
            return findTweetsFromRepository(new TimeGap(/* pTimeline.getOldestBound() */-1, -1));
        } else {
            return findTweetsFromServer(new TimeGap(-1, -1), 1, DEFAULT_PAGE_SIZE);
        }
    }

    public Observable<Observable<News>> findNewsInGap(final TimeGap pTimeGap) {
        return findTweetsFromServer(pTimeGap, DEFAULT_PAGE_COUNT, DEFAULT_PAGE_SIZE);
    }

    private Observable<Observable<News>> findTweetsFromRepository(final TimeGap pTimeGap) {
        return Observable.just(Observable.create(new Func1<Observer<News>, Subscription>() {
            public Subscription call(final Observer<News> pObserver) {
                try {
                    Query<DB_TWITTER> lQuery = Query.on(DB_TWITTER.values())
                                                    .selectAll(DB_TWITTER.VIEW_TIMELINE)
                                                    .from(DB_TWITTER.VIEW_TIMELINE)
                                                    .limit(DEFAULT_PAGE_SIZE);
                    if (pTimeGap.isFutureGap()) {
                        lQuery.whereGreater(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getOldestBound());
                    } else if (pTimeGap.isPastGap()) {
                        lQuery.whereLower(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getEarliestBound());
                    } else if (!pTimeGap.isInitialGap()) {
                        lQuery.whereGreater(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getOldestBound())
                              .whereLower(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.getEarliestBound());
                    }

                    lQuery.execute(mDatabase.getWritableDatabase(), new ResultHandler.Handle() {
                        public void handleRow(ResultHandler.Row pRow, Cursor pCursor) {
                            switch (mViewTimelineDAO.getKind(pRow)) {
                            case TWEET:
                                pObserver.onNext(mViewTimelineDAO.getTweet(pRow));
                                break;
                            case TIMEGAP:
                                pObserver.onNext(mViewTimelineDAO.getTimeGap(pRow));
                                break;
                            }
                        }
                    });
                    pObserver.onCompleted();
                } catch (Exception eException) {
                    pObserver.onError(eException);
                }
                return Subscriptions.empty();
            }
        }).cache());
    }

    private Observable<Observable<News>> findTweetsFromServer(final TimeGap pTimeGap, final int pPageCount, final int pPageSize) {
        Observable<Observable<Tweet>> lTweets = mTwitterAPI.getHome(pTimeGap, pPageCount, pPageSize);
        return lTweets.map(new Func1<Observable<Tweet>, Observable<News>>() {
            public Observable<News> call(Observable<Tweet> pTweets) {
                Observable<Tweet> lTweets = mDatabase.beginTransaction(pTweets);
                lTweets = commitTweet(lTweets);

                Observable<News> lTweetPage = commitTweetPage(pTimeGap, lTweets);
                return mDatabase.endTransaction(lTweetPage);
            }
        });
    }

    private Observable<Tweet> commitTweet(Observable<Tweet> pTweets) {
        return pTweets.map(new Action1<Tweet>() {
            public void call(Tweet pTweet) {
                mTweetDAO.create(pTweet);
            }
        });
    }

    private Observable<News> commitTweetPage(final TimeGap pTimeGap, final Observable<Tweet> pTweets) {
        return Observable.create(new Func1<Observer<News>, Subscription>() {
            public Subscription call(final Observer<News> pObserver) {
                return pTweets.subscribe(new Observer<Tweet>() {
                    private Tweet mLastTweet = null;

                    public void onNext(Tweet pTweet) {
                        mLastTweet = pTweet;
                        pObserver.onNext(pTweet);
                    }

                    public void onCompleted() {
                        if (mLastTweet != null) {
                            if (!pTimeGap.isFutureGap()) {
                                mTimeGapDAO.delete(pTimeGap);
                            }
                        } else if (!pTimeGap.isInitialGap() && !pTimeGap.isPastGap() && !pTimeGap.isFutureGap()) {
                            TimeGap lRemainingTimeGap = pTimeGap.substractEarlyBound(mLastTweet, DEFAULT_PAGE_SIZE);
                            mTimeGapDAO.update(lRemainingTimeGap);
                            pObserver.onNext(lRemainingTimeGap);
                        }
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
