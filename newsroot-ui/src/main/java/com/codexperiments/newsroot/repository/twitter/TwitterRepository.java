package com.codexperiments.newsroot.repository.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Func1;
import android.app.Application;
import android.database.Cursor;

import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.manager.twitter.Query;
import com.codexperiments.newsroot.manager.twitter.ResultHandler;
import com.codexperiments.newsroot.manager.twitter.TimeGapDAO;
import com.codexperiments.newsroot.manager.twitter.TweetDAO;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.COL_VIEW_TIMELINE;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.DB_TWITTER;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.manager.twitter.ViewTimelineDAO;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.robolabor.task.TaskManager;

public class TwitterRepository {
    private static final int DEFAULT_PAGE_COUNT = 5;
    public static final int DEFAULT_PAGE_SIZE = 20;

    private EventBus mEventBus;
    private TwitterAPI mTwitterAPI;

    private Map<String, Timeline> mFollowing;
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

        mFollowing = new HashMap<String, Timeline>();
        mDatabase = pDatabase;
        mTweetDAO = new TweetDAO(mDatabase);
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        mViewTimelineDAO = new ViewTimelineDAO(mDatabase);

        mDatabase.recreate();
    }

    public Timeline findTimeline(String pUsername) {
        Timeline lTimeline = mFollowing.get(pUsername);
        if (lTimeline != null) {
            lTimeline = new Timeline(pUsername);
        }
        return lTimeline;
    }

    public Observable<TweetPage> findLatestNews(Timeline pTimeline) {
        return findTweetsFromServer(new TimeGap(-1, pTimeline.getEarliestBound()), DEFAULT_PAGE_COUNT, DEFAULT_PAGE_SIZE);
    }

    public Observable<TweetPage> findOlderNewsFromCache(final Timeline pTimeline) {
        return findTweetsFromRepository(new TimeGap(pTimeline.getOldestBound(), -1));
    }

    public Observable<TweetPage> findOlderNews(final Timeline pTimeline) {
        if (pTimeline.hasMore()) {
            return findTweetsFromServer(new TimeGap(pTimeline.getOldestBound(), -1), 3, DEFAULT_PAGE_SIZE);
        } else {
            return Observable.empty();
        }
    }

    public Observable<TweetPage> findNewsInGap(final TimeGap pTimeGap) {
        return findTweetsFromServer(pTimeGap, DEFAULT_PAGE_COUNT, DEFAULT_PAGE_SIZE);
    }

    private Observable<TweetPage> findTweetsFromRepository(final TimeGap pTimeGap) {
        return Observable.create(new Func1<Observer<TweetPage>, Subscription>() {
            public Subscription call(final Observer<TweetPage> pObserver) {
                AndroidScheduler.threadPoolForDatabase().schedule(new Action0() {
                    public void call() {
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

                            final List<Tweet> lTweets = new ArrayList<Tweet>(DEFAULT_PAGE_SIZE);
                            lQuery.execute(mDatabase.getWritableDatabase(), new ResultHandler.Handle() {
                                public void handleRow(ResultHandler.Row pRow, Cursor pCursor) {
                                    switch (mViewTimelineDAO.getKind(pRow)) {
                                    case TWEET:
                                        lTweets.add(mViewTimelineDAO.getTweet(pRow));
                                        break;
                                    case TIMEGAP:
                                        // pObserver.onNext(mViewTimelineDAO.getTimeGap(pRow));
                                        break;
                                    }
                                }
                            });

                            pObserver.onNext(new TweetPage(lTweets, pTimeGap, DEFAULT_PAGE_SIZE));
                            pObserver.onCompleted();
                        } catch (Exception eException) {
                            pObserver.onError(eException);
                        }
                    }
                });
                return Subscriptions.empty();
            }
        });
        // .buffer(Integer.MAX_VALUE)
        // .observeOn(AndroidScheduler.getInstance())
        // .subscribe(new Observer<List<News>>() {
        // public void onNext(List<News> pArg0) {
        // }
        //
        // public void onCompleted() {
        // }
        //
        // public void onError(Throwable pArg0) {
        // }
        // });
    }

    private Observable<TweetPage> findTweetsFromServer(final TimeGap pTimeGap, final int pPageCount, final int pPageSize) {
        Observable<TweetPage> lTweetPage = mTwitterAPI.findHomeTweets(pTimeGap, pPageCount, pPageSize);
        lTweetPage.map(new Func1<TweetPage, TweetPage>() {
            public TweetPage call(TweetPage pTweetPage) {
                Observable<Tweet> lTweets = mDatabase.beginTransaction(Observable.from(pTweetPage));
                lTweets = commitTweetPage(lTweets, pTweetPage);
                lTweets = mDatabase.endTransaction(lTweets);
                return pTweetPage.apply(lTweets);
            }
        });
        return lTweetPage;
    }

    private Observable<Tweet> commitTweetPage(final Observable<Tweet> pTweets, final TweetPage pTweetPage) {
        return Observable.create(new Func1<Observer<Tweet>, Subscription>() {
            public Subscription call(final Observer<Tweet> pObserver) {
                return pTweets.subscribe(new Observer<Tweet>() {
                    public void onNext(Tweet pTweet) {
                        mTweetDAO.create(pTweet);
                        pObserver.onNext(pTweet);
                    }

                    public void onCompleted() {
                        TimeGap lRemainingTimeGap = pTweetPage.remainingGap();
                        if (lRemainingTimeGap != null) {
                            mTimeGapDAO.create(lRemainingTimeGap);
                        }
                        mTimeGapDAO.delete(pTweetPage.timeGap());
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
