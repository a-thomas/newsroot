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
import com.codexperiments.newsroot.common.rx.Property;
import com.codexperiments.newsroot.common.rx.Property.PropertyAccess;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.PageAdapterItem;

public class NewsTimeGapItem extends RelativeLayout implements PageAdapterItem<TimeGap> {
    // private Property<Boolean> mLoadingProperty;
    private AsyncCommand<TimeGap, TweetPageResponse> mFindGapCommand;

    private TimeGap mTimeGap;
    private TextView mUINewsCreatedAt;
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
    }

    @Override
    public void initialize(final TimeGap pTimeGap) {
        mTimeGap = pTimeGap;
        mSubcriptions = Subscriptions.create();

        mFindGapCommand = createFindGapCommand();
        mFindGapCommand.subscribe(new Action1<TweetPageResponse>() {
            public void call(TweetPageResponse pTweetPageResponse) {
                if (pTweetPageResponse != null) {
                    // TweetPage lPage = pTweetPageResponse.tweetPage();
                    // mTimeRange = TimeRange.append(mTimeRange, lPage.tweets());
                    // mTweets.insert(lPage);
                    Log.e("", "test1");
                } else {
                    Log.e("", "test2");
                }
            }
        });

        mSubcriptions.add(RxUI.fromClick(this).map(timegap()).subscribe(mFindGapCommand));
        mSubcriptions.add(mFindGapCommand.isRunning().subscribe(RxUI.toActivated(this)));
        mSubcriptions.add(mFindGapCommand.isRunning().map(RxUI.not()).subscribe(RxUI.toEnabled(this)));
    }

    @Override
    public void setContent(TimeGap pTimeGap) {
        mTimeGap = pTimeGap;

        mUINewsCreatedAt.setText(mTimeGap.earliestBound() + "==\n" + mTimeGap.oldestBound());
        // mFindGapCommand.reset();
    }

    protected AsyncCommand<TimeGap, TweetPageResponse> createFindGapCommand() {
        return AsyncCommand.create(new Func1<TimeGap, Observable<TweetPageResponse>>() {
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
    }

    protected Func1<Void, TimeGap> timegap() {
        return new Func1<Void, TimeGap>() {
            public TimeGap call(Void pVoid) {
                return mTimeGap;
            }
        };
    }

    protected Property<Boolean> loadingProperty() {
        return Property.create(new PropertyAccess<Boolean>() {
            public Boolean get() {
                return mTimeGap.isLoading();
            }

            public void set(Boolean pValue) {
                mTimeGap.setLoading(pValue);
            }
        });
    }
}
