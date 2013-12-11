package com.codexperiments.newsroot.repository.tweet;

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

import com.codexperiments.newsroot.data.tweet.TimeGapDAO;
import com.codexperiments.newsroot.data.tweet.TweetDAO;
import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.data.tweet.TweetHandler;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.Tweet;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class TweetDatabaseRepository implements TweetRepository {
    private TweetRepository mRepository;
    private TweetDatabase mDatabase;
    private Map<Timeline, Boolean> mHasMore; // TODO Concurrency
    private Map<String, Timeline> mFollowing;

    private TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;

    // private ViewTimelineDAO mViewTimelineDAO;

    public TweetDatabaseRepository(TweetDatabase pDatabase, TweetRepository pRepository) {
        super();
        mRepository = pRepository;
        mDatabase = pDatabase;
        mHasMore = new ConcurrentHashMap<Timeline, Boolean>(64);
        mFollowing = new HashMap<String, Timeline>();

        mTweetDAO = new TweetDAO(mDatabase);
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        // mViewTimelineDAO = new ViewTimelineDAO(mDatabase);

        mDatabase.recreate();
    }

    @Override
    public Timeline findTimeline(String pUsername) {
        Timeline lTimeline = mFollowing.get(pUsername);
        if (lTimeline == null) {
            lTimeline = new Timeline(pUsername);
            mFollowing.put(pUsername, lTimeline);
            mHasMore.put(lTimeline, Boolean.TRUE);
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
            return cacheTweets(mRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize));
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
                            final TweetHandler lTweetHandler = new TweetHandler();
                            List<TweetDTO> lTweets = mTweetDAO.find().byTimeGap(pTimeGap).limitTo(DEFAULT_PAGE_SIZE).asList();
                            // Some data was found in database.
                            if (lTweets.size() > 0) {
                                TweetPage lTweetPage = new TweetPage(lTweets, DEFAULT_PAGE_SIZE);
                                pObserver.onNext(new TweetPageResponse(lTweetPage, pTimeGap));
                                pObserver.onCompleted();
                            }
                            // No data was found in database, let's hope there are some in the cloud.
                            else {
                                mHasMore.put(pTimeline, Boolean.FALSE);
                                cacheTweets(mRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize)).subscribe(pObserver);
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

    private Observable<TweetPageResponse> cacheTweets(Observable<TweetPageResponse> pTweetPages) {
        final Observable<TweetPageResponse> lTweetPagesTransaction = mDatabase.beginTransaction(pTweetPages);

        final Observable<TweetPageResponse> lCachedTweetPages = Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pPageObserver) {
                return lTweetPagesTransaction.subscribe(new Observer<TweetPageResponse>() {
                    public void onNext(final TweetPageResponse pTweetPageResponse) {
                        Observable.from(pTweetPageResponse.tweetPage()) //
                                  .subscribe(cacheTweetsObserver(pTweetPageResponse, pPageObserver));
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

    private Observer<Tweet> cacheTweetsObserver(final TweetPageResponse pTweetPageResponse,
                                                final Observer<? super TweetPageResponse> pPageObserver)
    {
        return new Observer<Tweet>() {
            public void onNext(Tweet pTweet) {
                mTweetDAO.create(pTweet);
            }

            public void onCompleted() {
                mTimeGapDAO.delete(pTweetPageResponse.initialGap()); // TODO Bug here? Done after transaction?
                TimeGap lRemainingTimeGap = pTweetPageResponse.remainingGap();
                if (lRemainingTimeGap != null) {
                    mTimeGapDAO.create(lRemainingTimeGap);
                }
                pPageObserver.onNext(pTweetPageResponse);
            }

            public void onError(Throwable pThrowable) {
                pPageObserver.onError(pThrowable);
            }
        };
    }
}
