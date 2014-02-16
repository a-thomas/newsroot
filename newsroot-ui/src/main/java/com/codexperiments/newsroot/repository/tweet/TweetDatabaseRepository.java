package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.rx.Rxt.feedback;
import static rx.Observable.combineLatest;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Func2;

import com.codexperiments.newsroot.data.tweet.TimeGapDAO;
import com.codexperiments.newsroot.data.tweet.TweetDAO;
import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.rx.AndroidScheduler;
import com.codexperiments.rx.Rxt;
import com.codexperiments.rx.Rxt.FeedbackFunc;
import com.codexperiments.rx.Rxt.FeedbackOutput;

public class TweetDatabaseRepository implements TweetRepository {
    private TweetRemoteRepository mRemoteRepository;
    private TweetDatabase mDatabase;
    // private Map<Timeline, Boolean> mHasMore; // TODO Concurrency
    private Map<String, Timeline> mTimelines;

    @Inject TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;

    // private ViewTimelineDAO mViewTimelineDAO;

    public TweetDatabaseRepository(TweetDatabase pDatabase, TweetDAO pTweetDAO, TweetRemoteRepository pRemoteRepository) {
        super();
        mRemoteRepository = pRemoteRepository;
        mDatabase = pDatabase;
        // mHasMore = new ConcurrentHashMap<Timeline, Boolean>(64);
        mTimelines = new HashMap<String, Timeline>();

        mTweetDAO = pTweetDAO;
        mTimeGapDAO = new TimeGapDAO(mDatabase);
        // mViewTimelineDAO = new ViewTimelineDAO(mDatabase);

        mDatabase.recreate();
    }

    @Override
    public Timeline findTimeline(String pUsername) {
        Timeline lTimeline = mTimelines.get(pUsername);
        if (lTimeline == null) {
            lTimeline = new Timeline(pUsername);
            mTimelines.put(pUsername, lTimeline);
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
        FeedbackFunc<TimeGap, TweetPageResponse> lMergeFeedback = new FeedbackFunc<TimeGap, TweetPageResponse>() {
            public Observable<TimeGap> call(Observable<TimeGap> pInitialGap, Observable<TweetPageResponse> pTweetPageResponses) {

                return combineLatest(pInitialGap, pTweetPageResponses, new Func2<TimeGap, TweetPageResponse, TimeGap>() {
                    public TimeGap call(TimeGap pInitialURL, TweetPageResponse pTweetPageResponse) {
                        TimeGap lNextGap = pTimeGap;
                        if (pTweetPageResponse != null) {
                            TweetPage lTweetPage = pTweetPageResponse.tweetPage();
                            if (!lTweetPage.isFull()) return null;
                            else lNextGap = pTimeGap.remainingGap(lTweetPage.timeRange());
                        }
                        return lNextGap;
                    }
                }).takeWhile(Rxt.notNullValue());
            }
        };

        return feedback(pPageCount, lMergeFeedback, new FeedbackOutput<TimeGap, TweetPageResponse>() {
            public Observable<TweetPageResponse> call(Observable<TimeGap> pTimeGaps) {
                Observable<TweetPageResponse> fromDatabase = findTweetsInCache(pTimeline, pPageSize, pTimeGaps);
                Observable<TweetPageResponse> fromRemote = cacheTweets(mRemoteRepository.findTweets(pTimeline,
                                                                                                    pTimeGap,
                                                                                                    pPageCount,
                                                                                                    pPageSize));
                return Observable.concat(fromDatabase, fromRemote);
            }
        });
    }

    public Observable<TweetPageResponse> findTweetsIM(final Timeline pTimeline,
                                                      final TimeGap pTimeGap,
                                                      final int pPageCount,
                                                      final int pPageSize)
    {
        FeedbackFunc<TimeGap, TweetPageResponse> lMergeFeedback = new FeedbackFunc<TimeGap, TweetPageResponse>() {
            public Observable<TimeGap> call(Observable<TimeGap> pInitialGap, Observable<TweetPageResponse> pTweetPageResponses) {

                return combineLatest(pInitialGap, pTweetPageResponses, new Func2<TimeGap, TweetPageResponse, TimeGap>() {
                    public TimeGap call(TimeGap pInitialURL, TweetPageResponse pTweetPageResponse) {
                        TimeGap lNextGap = pTimeGap;
                        if (pTweetPageResponse != null) {
                            TweetPage lTweetPage = pTweetPageResponse.tweetPage();
                            if (!lTweetPage.isFull()) return null;
                            else lNextGap = pTimeGap.remainingGap(lTweetPage.timeRange());
                        }
                        return lNextGap;
                    }
                }).takeWhile(Rxt.notNullValue());
            }
        };

        return feedback(pPageCount, lMergeFeedback, new FeedbackOutput<TimeGap, TweetPageResponse>() {
            public Observable<TweetPageResponse> call(Observable<TimeGap> pTimeGaps) {
                Observable<TweetPageResponse> fromDatabase = findTweetsInCache(pTimeline, pPageSize, pTimeGaps);
                Observable<TweetPageResponse> fromRemote = cacheTweets(mRemoteRepository.findTweetsIM(pTimeline,
                                                                                                      pPageSize,
                                                                                                      pTimeGaps));
                return Observable.concat(fromDatabase, fromRemote);
            }
        });
    }

    public Observable<TweetPageResponse> findTweetsInCache(final Timeline pTimeline,
                                                           final int pPageSize,
                                                           final Observable<TimeGap> pTimeGaps)
    {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                return pTimeGaps.observeOn(AndroidScheduler.threadPoolForDatabase()).subscribe(new Observer<TimeGap>() {
                    public void onNext(TimeGap pTimeGap) {
                        TweetDTO[] lTweets = mTweetDAO.find()
                                                      .withTweets()
                                                      .byTimeGap(pTimeGap)
                                                      .limitTo(DEFAULT_PAGE_SIZE)
                                                      .asArray();
                        TweetPage lTweetPage = new TweetPage(lTweets, DEFAULT_PAGE_SIZE);
                        TweetPageResponse lTweetPageResponse = new TweetPageResponse(lTweetPage, pTimeGap);
                        int lTweetCount = lTweets.length;
                        // if (lTweetCount > 0) {
                        pObserver.onNext(lTweetPageResponse);
                        // }
                        // if (lTweetCount < DEFAULT_PAGE_SIZE) {
                        // pObserver.onCompleted();
                        // }
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

    private Observable<TweetPageResponse> findTweetsFromRemote(final Timeline pTimeline,
                                                               final int pPageCount,
                                                               final int pPageSize,
                                                               final Observable<TimeGap> pTimeGaps)
    {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                return pTimeGaps.subscribe(new Observer<TimeGap>() {
                    public void onNext(TimeGap pTimeGap) {
                        // if ((pPageCount > 1) && (lTweetPage.isEmpty())) {
                        // // TimeGap lTimeGap = pTweetPageResponse.remainingGap();
                        // cacheTweets(mRemoteRepository.findTweets(pTimeline, //
                        // pTimeGap,
                        // pPageCount,
                        // pPageSize)).subscribe(pObserver);
                        // }
                    }

                    public void onCompleted() {
                        pObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        pObserver.onError(pThrowable);
                    }
                });
            }
        }).subscribeOn(AndroidScheduler.threadPoolForDatabase());
    }

    private Observable<TweetPageResponse> findTweetsFromRemote2(final Timeline pTimeline,
                                                                final int pPageCount,
                                                                final int pPageSize,
                                                                final Observable<TweetPageResponse> pTweetPageResponses)
    {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                return pTweetPageResponses.subscribe(new Observer<TweetPageResponse>() {
                    public void onNext(TweetPageResponse pTweetPageResponse) {
                        // try {
                        // } catch (Exception eException) {
                        // pObserver.onError(eException);
                        // }
                        TweetPage lTweetPage = pTweetPageResponse.tweetPage();
                        if ((pPageCount > 1) && (lTweetPage.isEmpty())) {
                            TimeGap lTimeGap = pTweetPageResponse.remainingGap();
                            cacheTweets(mRemoteRepository.findTweets(pTimeline, //
                                                                     lTimeGap,
                                                                     pPageCount,
                                                                     pPageSize)).subscribe(pObserver);
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
        }).subscribeOn(AndroidScheduler.threadPoolForDatabase());
    }

    public Observable<TweetPageResponse> findTweetsBck(final Timeline pTimeline,
                                                       final TimeGap pTimeGap,
                                                       final int pPageCount,
                                                       final int pPageSize)
    {
        return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                findCachedTweets(pObserver, pTimeline, pTimeGap, pPageCount, pPageSize, true);
                return Subscriptions.empty();
            }
        }).subscribeOn(AndroidScheduler.threadPoolForDatabase());
    }

    private Subscription findCachedTweets(final Observer<? super TweetPageResponse> pObserver,
                                          final Timeline pTimeline,
                                          final TimeGap pTimeGap,
                                          final int pPageCount,
                                          final int pPageSize,
                                          final boolean pFindFromSource)
    {
        try {
            // TODO final TweetHandler lTweetHandler = new TweetHandler();
            TweetDTO[] lTweets = mTweetDAO.find().withTweets().byTimeGap(pTimeGap).limitTo(DEFAULT_PAGE_SIZE).asArray();
            // Either we won't look for server data even if we have some (in which case we may an empty page)
            // or some data was found in database (because if not we will look for data from the server).
            if (!pFindFromSource || (lTweets.length > 0)) {
                TweetPage lTweetPage = new TweetPage(lTweets, DEFAULT_PAGE_SIZE);
                TweetPageResponse lTweetPageResponse = new TweetPageResponse(lTweetPage, pTimeGap);
                pObserver.onNext(lTweetPageResponse);

                if ((pPageCount > 1) && (lTweets.length >= DEFAULT_PAGE_SIZE)) { // TODO Page full
                    findCachedTweets(pObserver, pTimeline, lTweetPageResponse.remainingGap(), pPageCount - 1, pPageSize, false);
                } else {
                    pObserver.onCompleted();
                }
            }
            // No data was found in database, let's hope there are some in the cloud.
            else {
                cacheTweets(mRemoteRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize)).subscribe(pObserver);
            }
        } catch (Exception eException) {
            pObserver.onError(eException);
        }
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
                if (pTweetPageResponse.initialGap() != null) { // TODO REMOVE
                    mTimeGapDAO.delete(pTweetPageResponse.initialGap()); // TODO Bug
                }
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
