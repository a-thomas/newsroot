package com.codexperiments.newsroot.repository.tweet;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class TweetDatabaseRepository implements TweetRepository {
    private TweetRepository mRepository;
    private TweetDatabase mDatabase;
    // private Map<Timeline, Boolean> mHasMore; // TODO Concurrency
    private Map<String, Timeline> mFollowing;

    @Inject TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;

    // private ViewTimelineDAO mViewTimelineDAO;

    public TweetDatabaseRepository(TweetDatabase pDatabase, TweetDAO pTweetDAO, TweetRepository pRepository) {
        super();
        mRepository = pRepository;
        mDatabase = pDatabase;
        // mHasMore = new ConcurrentHashMap<Timeline, Boolean>(64);
        mFollowing = new HashMap<String, Timeline>();

        mTweetDAO = pTweetDAO;
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
            // mHasMore.put(lTimeline, Boolean.TRUE);
        }
        return lTimeline;
    }

    @Override
    public Observable<TweetPageResponse> findTweets(final Timeline pTimeline,
                                                    final TimeGap pTimeGap,
                                                    final int pPageCount,
                                                    final int pPageSize)
    {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                findCachedTweets(pObserver, pTimeline, pTimeGap, pPageCount, pPageSize, true);
                return Subscriptions.empty();
            }
        });
    }

    private Subscription findCachedTweets(final Observer<? super TweetPageResponse> pObserver,
                                          final Timeline pTimeline,
                                          final TimeGap pTimeGap,
                                          final int pPageCount,
                                          final int pPageSize,
                                          final boolean pFindFromSource)
    {
        AndroidScheduler.threadPoolForDatabase().schedule(new Action0() {
            public void call() {
                try {
                    // TODO final TweetHandler lTweetHandler = new TweetHandler();
                    TweetDTO[] lTweets = mTweetDAO.find().withTweets().byTimeGap(pTimeGap).limitTo(DEFAULT_PAGE_SIZE).asArray();
                    // Either we won't look for server data even if we have some (in which case we may an empty page)
                    // or some data was found in database (because if not we will look for data from the server).
                    if (!pFindFromSource || (lTweets.length > 0)) {
                        TweetPage lTweetPage = new TweetPage(lTweets, DEFAULT_PAGE_SIZE);
                        TweetPageResponse lTweetPageResponse = new TweetPageResponse(lTweetPage, pTimeGap);
                        pObserver.onNext(lTweetPageResponse);

                        if ((pPageCount > 1) && (lTweets.length >= DEFAULT_PAGE_SIZE)) {
                            findCachedTweets(pObserver,
                                             pTimeline,
                                             lTweetPageResponse.remainingGap(),
                                             pPageCount - 1,
                                             pPageSize,
                                             false);
                        } else {
                            pObserver.onCompleted();
                        }
                    }
                    // No data was found in database, let's hope there are some in the cloud.
                    else {
                        cacheTweets(mRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize)).subscribe(pObserver);
                    }
                } catch (Exception eException) {
                    pObserver.onError(eException);
                }
            }
        });
        return Subscriptions.empty();
    }

    private Observable<TweetPageResponse> cacheTweets(Observable<TweetPageResponse> pTweetPages) {
        final Observable<TweetPageResponse> lTweetPagesTransaction = mDatabase.beginTransaction(pTweetPages);

        final Observable<TweetPageResponse> lCachedTweetPages = Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pPageObserver) {
                return lTweetPagesTransaction.subscribe(new Observer<TweetPageResponse>() {
                    public void onNext(final TweetPageResponse pTweetPageResponse) {
                        Observable.from(pTweetPageResponse.tweetPage().tweets()) //
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

    private Observer<TweetDTO> cacheTweetsObserver(final TweetPageResponse pTweetPageResponse,
                                                   final Observer<? super TweetPageResponse> pPageObserver)
    {
        return new Observer<TweetDTO>() {
            public void onNext(TweetDTO pTweet) {
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
