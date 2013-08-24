package com.codexperiments.newsroot.repository.twitter;

import java.util.List;

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
import com.codexperiments.rx.ObservablePage;

public class TwitterRepository {
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

    public ObservablePage<? extends News> findLatestTweets(Timeline pTimeline) {
        // List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        // pTimeline.appendOldItems(lResult);
        // lResult = findTweetsInGap(new TimeGap(-1, pTimeline.getEarliestBound()));
        // pTimeline.appendNewItems(lResult);
        // return lResult;
        // Observable<News> lTweetsFromCache = findTweetsFromCache(new TimeGap());
        return findTweetsFromServer(new TimeGap(-1, -1));
        // return Pair.create(Observable.concat(lTweetsFromNetwork.first, lTweetsFromCache), lTweetsFromNetwork.second);
        // return Pair.create(Observable.concat(lTweetsFromCache, lTweetsFromNetwork.first), lTweetsFromNetwork.second);
    }

    public ObservablePage<? extends News> findOlderTweets(Timeline pTimeline) {
        // List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        // pTimeline.appendOldItems(lResult);
        // return lResult;
        return findTweetsFromServer(new TimeGap(/* pTimeline.getOldestBound() */-1, -1));
    }

    public ObservablePage<? extends News> findTweetsInGap(final TimeGap pTimeGap) {
        return findTweetsFromServer(pTimeGap);
    }

    private Observable<News> findTweetsFromRepository(final TimeGap pTimeGap) {
        return Observable.create(new Func1<Observer<News>, Subscription>() {
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
        });
    }

    private ObservablePage<? extends News> findTweetsFromServer(final TimeGap pTimeGap) {
        final Action1<List<Tweet>> updateTimeline = new Action1<List<Tweet>>() {
            public void call(List<Tweet> pTweets) {
                if (pTweets.size() == 0) {
                    if (!pTimeGap.isFutureGap()) {
                        mTimeGapDAO.delete(pTimeGap);
                    }
                } else if (!pTimeGap.isInitialGap() && !pTimeGap.isPastGap() && !pTimeGap.isFutureGap()) {
                    TimeGap lRemainingTimeGap = pTimeGap.substract(pTweets, DEFAULT_PAGE_SIZE);
                    mTimeGapDAO.update(lRemainingTimeGap);
                }
            }
        };
        final Action1<Tweet> createTweet = new Action1<Tweet>() {
            public void call(Tweet pTweet) {
                mTweetDAO.create(pTweet);
            }
        };

        ObservablePage<Tweet> lTweets = mTwitterAPI.getHome(pTimeGap, DEFAULT_PAGE_SIZE);
        Observable<Tweet> lCommitedTweets = mDatabase.doInTransaction(lTweets, createTweet, updateTimeline);
        return ObservablePage.create(lCommitedTweets, lTweets);
    }
}
