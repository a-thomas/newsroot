package com.codexperiments.newsroot.repository.twitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import android.app.Application;
import android.database.Cursor;

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
import com.codexperiments.newsroot.manager.twitter.ViewTimelineDAO;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class TwitterDatabaseRepository implements TwitterRepository {
    private TwitterRepository mRepository;
    private TwitterDatabase mDatabase;
    private Map<Timeline, Boolean> mHasMore; // TODO Concurrency
    private Map<String, Timeline> mFollowing;

    private TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;
    private ViewTimelineDAO mViewTimelineDAO;

    public TwitterDatabaseRepository(Application pApplication, TwitterDatabase pDatabase, TwitterRepository pRepository) {
        super();
        mDatabase = pDatabase;
        mHasMore = new ConcurrentHashMap<Timeline, Boolean>(64);
        mFollowing = new HashMap<String, Timeline>();

        mTweetDAO = new TweetDAO(mDatabase);
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        mViewTimelineDAO = new ViewTimelineDAO(mDatabase);

        mDatabase.recreate();
    }

    @Override
    public Timeline findTimeline(String pUsername) {
        Timeline lTimeline = mFollowing.get(pUsername);
        if (lTimeline != null) {
            lTimeline = new Timeline(pUsername);
        }
        return lTimeline;
    }

    @Override
    public Observable<TweetPageResponse> findTweets(Timeline pTimeline, TimeGap pTimeGap, int pPageCount, int pPageSize) {
        Boolean lHasMore = mHasMore.get(pTimeline);
        if (lHasMore) {
            lHasMore = Boolean.TRUE;
            mHasMore.put(pTimeline, lHasMore);
        }

        if (pTimeGap.isPastGap() && lHasMore == Boolean.TRUE) {
            return findCachedTweets(pTimeline, pTimeGap, pPageCount, pPageSize);
        } else {
            return cacheTweetPages(mRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize));
        }
    }

    private Observable<TweetPageResponse> findCachedTweets(final Timeline pTimeline,
                                                           final TimeGap pTimeGap,
                                                           final int pPageCount,
                                                           final int pPageSize)
    {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                AndroidScheduler.threadPoolForDatabase().schedule(new Action0() {
                    public void call() {
                        try {
                            Query<DB_TWITTER> lQuery = Query.on(DB_TWITTER.values())
                                                            .selectAll(DB_TWITTER.VIEW_TIMELINE)
                                                            .from(DB_TWITTER.VIEW_TIMELINE)
                                                            .limit(DEFAULT_PAGE_SIZE);
                            if (pTimeGap.isFutureGap()) {
                                lQuery.whereGreater(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.oldestBound());
                            } else if (pTimeGap.isPastGap()) {
                                lQuery.whereLower(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.earliestBound());
                            } else if (!pTimeGap.isInitialGap()) {
                                lQuery.whereGreater(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.oldestBound())
                                      .whereLower(COL_VIEW_TIMELINE.VIEW_TIMELINE_ID, pTimeGap.earliestBound());
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

                            if (lTweets.size() > 0) {
                                TweetPage lTweetPage = new TweetPage(lTweets, DEFAULT_PAGE_SIZE);
                                pObserver.onNext(new TweetPageResponse(lTweetPage, pTimeGap));
                                pObserver.onCompleted();
                            } else {
                                mHasMore.put(pTimeline, Boolean.FALSE);
                                cacheTweetPages(mRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize)).subscribe(pObserver);
                            }
                        } catch (Exception eException) {
                            pObserver.onError(eException);
                        }
                    }
                });
                return Subscriptions.empty();
            }
        });
    }

    private Observable<TweetPageResponse> cacheTweetPages(Observable<TweetPageResponse> pTweetPages) {
        final Observable<TweetPageResponse> lTweetPagesTransaction = mDatabase.beginTransaction(pTweetPages);

        final Observable<TweetPageResponse> lCachedTweetPages = Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pPageObserver) {
                return lTweetPagesTransaction.subscribe(new Observer<TweetPageResponse>() {
                    public void onNext(final TweetPageResponse pTweetPageResponse) {
                        Observable.from(pTweetPageResponse.tweetPage()).subscribe(new Observer<Tweet>() {
                            public void onNext(Tweet pTweet) {
                                mTweetDAO.create(pTweet);
                            }

                            public void onCompleted() {
                                mTimeGapDAO.delete(pTweetPageResponse.initialGap());
                                TimeGap lRemainingTimeGap = pTweetPageResponse.remainingGap();
                                if (lRemainingTimeGap != null) {
                                    mTimeGapDAO.create(lRemainingTimeGap);
                                }
                                pPageObserver.onNext(pTweetPageResponse);
                            }

                            public void onError(Throwable pThrowable) {
                                pPageObserver.onError(pThrowable);
                            }
                        });
                    }

                    public void onCompleted() {
                        pPageObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        pPageObserver.onError(pThrowable);
                    }
                });
            }
        });
        return mDatabase.endTransaction(lCachedTweetPages);
    }
}
