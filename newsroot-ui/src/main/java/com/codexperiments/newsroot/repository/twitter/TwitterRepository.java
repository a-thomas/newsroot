package com.codexperiments.newsroot.repository.twitter;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.BufferClosing;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.app.Application;
import android.database.Cursor;
import android.util.Pair;

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
    private static final int DEFAULT_PAGE_SIZE = 5;

    // private Config mConfig;
    private EventBus mEventBus;
    // private TaskManager mTaskManager;
    // private TwitterManager mTwitterManager;
    private TwitterAPI mTwitterAPI;

    // private Set<TweetListener> mListeners;

    public interface TweetListener {
        void onNewsLoaded(List<News> pItems);
    }

    // public void register(TweetListener pTweetListener) {
    // mListeners.add(pTweetListener);
    // }
    //
    // public void unregister(TweetListener pTweetListener) {
    // mListeners.remove(pTweetListener);
    // }

    private TwitterDatabase mDatabase;
    private TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;
    private ViewTimelineDAO mViewTimelineDAO;

    public TwitterRepository(Application pApplication,
                             EventBus pEventBus,
                             TaskManager pTaskManager,
                             TwitterManager pTwitterManager,
                             TwitterAPI pTwitterAPI,
                             TwitterDatabase pDatabase,
                             Config pConfig)
    {
        super();
        // mConfig = pConfig;
        mEventBus = pEventBus;
        mEventBus.registerListener(this);
        // mTaskManager = pTaskManager;
        // mTwitterManager = pTwitterManager;
        mTwitterAPI = pTwitterAPI;

        mDatabase = pDatabase;
        mTweetDAO = new TweetDAO(mDatabase);
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        mViewTimelineDAO = new ViewTimelineDAO(mDatabase);

        // mListeners = new HashSet<TwitterRepository.TweetListener>();
    }

    public Pair<Observable<News>, Observable<BufferClosing>> findLatestTweets(Timeline pTimeline) {
        // List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        // pTimeline.appendOldItems(lResult);
        // lResult = findTweetsInGap(new TimeGap(-1, pTimeline.getEarliestBound()));
        // pTimeline.appendNewItems(lResult);
        // return lResult;
        // Observable<News> lTweetsFromCache = findTweetsFromCache(new TimeGap());
        Pair<Observable<News>, Observable<BufferClosing>> lTweetsFromNetwork = findTweetsFromNetwork(new TimeGap(-1, -1));
        return Pair.create(lTweetsFromNetwork.first, lTweetsFromNetwork.second);
        // return Pair.create(Observable.concat(lTweetsFromNetwork.first, lTweetsFromCache), lTweetsFromNetwork.second);
        // return Pair.create(Observable.concat(lTweetsFromCache, lTweetsFromNetwork.first), lTweetsFromNetwork.second);
    }

    public Pair<Observable<News>, Observable<BufferClosing>> findOlderTweets(Timeline pTimeline) {
        // List<News> lResult = findTweets(new TimeGap(pTimeline.getOldestBound(), -1));
        // pTimeline.appendOldItems(lResult);
        // return lResult;
        return findTweetsFromNetwork(new TimeGap(/* pTimeline.getOldestBound() */-1, -1));
    }

    public Pair<Observable<News>, Observable<BufferClosing>> findTweetsInGap(final TimeGap pTimeGap) {
        return findTweetsFromNetwork(pTimeGap);
    }

    private Observable<News> findTweetsFromCache(final TimeGap pTimeGap) {
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

    private Pair<Observable<News>, Observable<BufferClosing>> findTweetsFromNetwork(final TimeGap pTimeGap) {
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
        final Action1<Tweet> createTweets = new Action1<Tweet>() {
            public void call(Tweet pTweet) {
                mTweetDAO.create(pTweet);
            }
        };

        Pair<Observable<Tweet>, Observable<BufferClosing>> lTweetPair = mTwitterAPI.getHome(pTimeGap, DEFAULT_PAGE_SIZE);
        Observable<Tweet> lTweets = lTweetPair.first;
        Observable<Tweet> lCommitedTweets = mTwitterAPI.commitTweets(lTweets,
                                                                     lTweetPair.second,
                                                                     mDatabase,
                                                                     createTweets,
                                                                     updateTimeline);
        return new Pair<Observable<News>, Observable<BufferClosing>>(mTwitterAPI.downcast(lCommitedTweets, News.class),
                                                                     lTweetPair.second);
    }

    public interface Config {
        String getHost();

        String getCallbackURL();
    }
}
