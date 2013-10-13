package com.codexperiments.newsroot.presentation;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.util.Log;

import com.codexperiments.newsroot.common.rx.AsyncCommand;
import com.codexperiments.newsroot.common.rx.Command;
import com.codexperiments.newsroot.common.rx.Property2;
import com.codexperiments.newsroot.common.rx.Property2.PropertyProxy;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class TimeGapPresentation {
    public static class Model implements NewsPresentation {
        public TimeGap mTimeGap;
        public Boolean mLoading;

        public Model(TimeGap pTimeGap) {
            this(pTimeGap, Boolean.FALSE);
        }

        public Model(TimeGap pTimeGap, Boolean pBoolean) {
            super();
            mTimeGap = pTimeGap;
            mLoading = pBoolean;
        }
    }

    public Model mModel;

    private Property2<Boolean> mLoadingProperty;
    private final Command<Void, Void> mLoadCommand;
    private AsyncCommand<TimeGap, TweetPageResponse> mFindGapCommand;

    @SuppressWarnings("unchecked")
    public TimeGapPresentation(Model pTimeGapPresentation) {
        super();
        mModel = pTimeGapPresentation;
        mLoadingProperty = Property2.create(new PropertyProxy<Boolean>() {
            public Boolean get() {
                return mModel.mLoading;
            }

            public void set(Boolean pValue) {
                mModel.mLoading = pValue;
            }
        });

        mLoadCommand = Command.create();
        mFindGapCommand = findGapCommand();

        mLoadingProperty.where(RxUI.eq(Boolean.TRUE)) //
                        .map(new Func1<Boolean, TimeGap>() {
                            public TimeGap call(Boolean pValue) {
                                return mModel.mTimeGap;
                            }
                        })
                        .subscribe(mFindGapCommand);

        Observable<Boolean> loadingRequested = mFindGapCommand.where(matchesTimegap()).map(RxUI.toConstant(Boolean.FALSE));
        Observable<Boolean> loadingFinished = mLoadCommand.map(RxUI.toConstant(Boolean.TRUE));
        Observable.merge(loadingRequested, loadingFinished).subscribe(mLoadingProperty); // TODO distinct.
    }

    public void bind(Model pModel) {
        mModel = pModel;
        mLoadingProperty.set(mModel.mLoading);
    }

    public AsyncCommand<TimeGap, TweetPageResponse> findGapCommand() {
        if (mFindGapCommand == null) {
            mFindGapCommand = AsyncCommand.create(new Func1<TimeGap, Observable<TweetPageResponse>>() {
                public Observable<TweetPageResponse> call(final TimeGap pTimeGap) {
                    return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
                        public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                            AndroidScheduler.threadPoolForIO().schedule(new Action0() {
                                public void call() {
                                    try {
                                        Thread.sleep(5000);
                                        pObserver.onNext(null);
                                        pObserver.onCompleted();
                                    } catch (InterruptedException e) {
                                    }
                                }
                            });
                            return Subscriptions.empty();
                        }
                    });
                }
            });

            mFindGapCommand.subscribe(new Action1<TweetPageResponse>() {
                public void call(TweetPageResponse pTweetPageResponse) {
                    if (pTweetPageResponse != null) {
                        // TweetPage lPage = pTweetPageResponse.tweetPage();
                        // mTimeRange = TimeRange.append(mTimeRange, lPage.tweets());
                        // mTweets.insert(lPage);
                        Log.e("", mModel.mTimeGap.toString());
                    } else {
                        Log.e("", mModel.mTimeGap.toString());
                    }
                }
            });
        }
        return mFindGapCommand;
    }

    private Func1<TweetPageResponse, Boolean> matchesTimegap() {
        return new Func1<TweetPageResponse, Boolean>() {
            public Boolean call(TweetPageResponse pTweetPageResponse) {
                return (pTweetPageResponse != null) ? pTweetPageResponse.initialGap().equals(mModel.mTimeGap) : Boolean.TRUE;
            }
        };
    }

    public TimeGap getTimeGap() {
        return mModel.mTimeGap;
    }

    public Command<Void, ?> loadCommand() {
        return mLoadCommand;
    }

    public Property2<Boolean> loading() {
        return mLoadingProperty;
    }
}