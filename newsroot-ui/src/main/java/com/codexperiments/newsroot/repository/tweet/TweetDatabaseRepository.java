package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.rx.RxAndroid.asArray;
import static com.codexperiments.rx.RxAndroid.beginTransaction;
import static com.codexperiments.rx.RxAndroid.endTransaction;
import static com.codexperiments.rx.RxAndroid.select;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.util.functions.Action1;
import rx.util.functions.Func0;
import rx.util.functions.Func1;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.codexperiments.newsroot.common.data.Query;
import com.codexperiments.newsroot.data.tweet.TimeGapDAO;
import com.codexperiments.newsroot.data.tweet.TweetDAO;
import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.DB_TWEET;
import com.codexperiments.newsroot.data.tweet.TweetHandler;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.rx.Rxt;

public class TweetDatabaseRepository implements TweetRepository {
    private TweetRemoteRepository mRemoteRepository;
    private TweetDatabase mDatabase;
    private Map<String, Timeline> mTimelines;

    private TweetDAO mTweetDAO;
    private TimeGapDAO mTimeGapDAO;

    @Inject
    public TweetDatabaseRepository(TweetDatabase pDatabase, TweetDAO pTweetDAO, TweetRemoteRepository pRemoteRepository) {
        super();
        mRemoteRepository = pRemoteRepository;
        mDatabase = pDatabase;
        mTimelines = new HashMap<String, Timeline>();

        mTweetDAO = pTweetDAO;
        mTimeGapDAO = new TimeGapDAO(mDatabase);

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
        return Observable.defer(new Func0<Observable<? extends TweetPageResponse>>() {
            public Observable<? extends TweetPageResponse> call() {
                final BehaviorSubject<TimeGap> lTimeGaps = BehaviorSubject.create(pTimeGap);

                Observable<TweetPageResponse> lFromDatabase = findTweetsInCache(pTimeline, pPageSize, lTimeGaps);
                lFromDatabase = Rxt.takeWhileInclusive(lFromDatabase, PAGE_FULL).filter(PAGE_NOT_EMPTY);

                Observable<TweetPageResponse> lFromRepo = cacheTweets(mRemoteRepository.findTweets(null, pPageSize, lTimeGaps));
                lFromRepo = Rxt.takeWhileInclusive(lFromRepo, PAGE_FULL).filter(PAGE_NOT_EMPTY);

                return Observable.concat(lFromDatabase, lFromRepo) //
                                 .take(pPageCount)
                                 .doOnNext(new Action1<TweetPageResponse>() {
                                     public void call(TweetPageResponse pTweetPageResponse) {
                                         lTimeGaps.onNext(pTimeGap.remainingGap(pTweetPageResponse.tweetPage().timeRange()));
                                     }
                                 });
            }
        });
    }

    public Observable<TweetPageResponse> findTweetsInCache(final Timeline pTimeline,
                                                           final int pPageSize,
                                                           final Observable<TimeGap> pTimeGaps)
    {
        pTimeGaps.map(new Func1<TimeGap, Query<DB_TWEET>>() {
            public Query<DB_TWEET> call(TimeGap pTimeGap) {
                return mTweetDAO.find().selectTweets().byTimeGap(pTimeGap).limitTo(DEFAULT_PAGE_SIZE).asQuery();
            }
        });
        Observable<Cursor> lSelectCursor = select(mDatabase.getConnection(), pTimeGaps.map(new Func1<TimeGap, Query<DB_TWEET>>() {
            public Query<DB_TWEET> call(TimeGap pTimeGap) {
                return mTweetDAO.find().selectTweets().byTimeGap(pTimeGap).limitTo(DEFAULT_PAGE_SIZE).asQuery();
            }
        }));
        return asArray(lSelectCursor, new TweetHandler()).map(new Func1<TweetDTO[], TweetPageResponse>() {
            public TweetPageResponse call(TweetDTO[] pTweets) {
                return new TweetPageResponse(new TweetPage(pTweets, DEFAULT_PAGE_SIZE), null);
            }
        });
        // return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
        // public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
        // return pTimeGaps.observeOn(AndroidScheduler.threadPoolForDatabase()).subscribe(new Observer<TimeGap>() {
        // public void onNext(TimeGap pTimeGap) {
        // TweetDTO[] lTweets = mTweetDAO.find()
        // .selectTweets()
        // .byTimeGap(pTimeGap)
        // .limitTo(DEFAULT_PAGE_SIZE)
        // .asArray();
        // TweetPage lTweetPage = new TweetPage(lTweets, DEFAULT_PAGE_SIZE);
        // TweetPageResponse lTweetPageResponse = new TweetPageResponse(lTweetPage, pTimeGap);
        // pObserver.onNext(lTweetPageResponse);
        // }
        //
        // public void onCompleted() {
        // pObserver.onCompleted();
        // }
        //
        // public void onError(Throwable pThrowable) {
        // pObserver.onError(pThrowable);
        // }
        // });
        // }
        // });
    }

    private Observable<TweetPageResponse> cacheTweets(Observable<TweetPageResponse> pTweetPages) {
        SQLiteDatabase lConnection = mDatabase.getWritableDatabase();
        final Observable<TweetPageResponse> lTweetPagesTransaction = beginTransaction(lConnection, pTweetPages);

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
        return endTransaction(lConnection, lCachedTweetPages);
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

    private static final Func1<TweetPageResponse, Boolean> PAGE_FULL = new Func1<TweetPageResponse, Boolean>() {
        public Boolean call(TweetPageResponse pTweetPageResponse) {
            return pTweetPageResponse != null && pTweetPageResponse.tweetPage().isFull();
        }
    };
    private static final Func1<TweetPageResponse, Boolean> PAGE_NOT_EMPTY = new Func1<TweetPageResponse, Boolean>() {
        public Boolean call(TweetPageResponse pTweetPageResponse) {
            return pTweetPageResponse != null && !pTweetPageResponse.tweetPage().isEmpty();
        }
    };
}
