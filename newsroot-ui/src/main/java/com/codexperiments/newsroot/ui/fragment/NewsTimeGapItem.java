package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.rx.AsyncCommand;
import com.codexperiments.newsroot.common.rx.Command;
import com.codexperiments.newsroot.common.rx.Property2;
import com.codexperiments.newsroot.common.rx.Property2.PropertyProxy;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.presentation.NewsPresentation;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.PageAdapterItem;

public class NewsTimeGapItem extends RelativeLayout implements PageAdapterItem<NewsTimeGapItem.Model> {
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
    private/* final */Command<Void, Void> mLoadCommand;
    private AsyncCommand<TimeGap, TweetPageResponse> mFindGapCommand;

    private TextView mUINewsCreatedAt;
    // private TimeGapPresentation mPresentation;
    private CompositeSubscription mSubcriptions;

    public NewsTimeGapItem(Context pContext, AttributeSet pAttrSet, int pDefStyle) {
        super(pContext, pAttrSet, pDefStyle);
    }

    public NewsTimeGapItem(Context pContext, AttributeSet pAttrSet) {
        super(pContext, pAttrSet);
    }

    public NewsTimeGapItem(Context pContext) {
        super(pContext);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_timegap);
        mSubcriptions = Subscriptions.create();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setContent(NewsTimeGapItem.Model pTimeGapPresentation) {
        // if (mPresentation != null) {
        // mSubcriptions.unsubscribe();
        // mSubcriptions = Subscriptions.create();
        // }
        if (mLoadingProperty == null) {
            mModel = pTimeGapPresentation;
            mLoadCommand = Command.create();
            mFindGapCommand = findGapCommand();
            mLoadingProperty = Property2.create(new PropertyProxy<Boolean>() {
                public Boolean get() {
                    return mModel.mLoading;
                }

                public void set(Boolean pValue) {
                    mModel.mLoading = pValue;
                }
            });
            mLoadingProperty.where(RxUI.eq(Boolean.TRUE)) //
                            .map(new Func1<Boolean, TimeGap>() {
                                public TimeGap call(Boolean pValue) {
                                    return mModel.mTimeGap;
                                }
                            })
                            .subscribe(mFindGapCommand);

            // mPresentation = new TimeGapPresentation(pTimeGapPresentation);
            mSubcriptions.add(RxUI.fromClick(this).subscribe(mLoadCommand));
            // mPresentation.isSelected().subscribe(RxUI.toActivated(this));
            mSubcriptions.add(mLoadingProperty.subscribe(RxUI.toActivated(this)));
            mSubcriptions.add(mLoadingProperty.map(RxUI.not()).subscribe(RxUI.toEnabled(this)));

            Observable<Boolean> loadingRequested = mFindGapCommand./* where(matchesTimegap()). */map(RxUI.toConstant(Boolean.FALSE));
            Observable<Boolean> loadingFinished = mLoadCommand.map(RxUI.toConstant(Boolean.TRUE));
            Observable.merge(loadingRequested, loadingFinished).subscribe(mLoadingProperty); // TODO distinct.
        } else {
            mModel = pTimeGapPresentation;
            mLoadingProperty.set(mModel.mLoading);
            // mPresentation.bind(pTimeGapPresentation);
        }

        TimeGap lTimeGap = pTimeGapPresentation.mTimeGap;
        mUINewsCreatedAt.setText(lTimeGap.earliestBound() + "==\n" + lTimeGap.oldestBound());
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
}
