package com.codexperiments.newsroot.ui.fragment;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.domain.tweet.TimeGap;

public class NewsTimeGapItem extends RelativeLayout {
    // private Property<Boolean> mLoadingProperty;
    // private AsyncCommand<TimeGap, TweetPageResponse> mFindGapCommand;

    // private TimeGap mTimeGap;
    private TextView mUINewsCreatedAt;

    // private CompositeSubscription mSubcriptions;

    public static NewsTimeGapItem create(FragmentActivity pActivity, ViewGroup pParent) {
        return (NewsTimeGapItem) pActivity.getLayoutInflater().inflate(R.layout.item_news_timegap, pParent, false);
    }

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

    // @Override
    // public void initialize(final TimeGap pTimeGap) {
    // mTimeGap = pTimeGap;
    // mSubcriptions = Subscriptions.create();
    //
    // mFindGapCommand = createFindGapCommand();
    // mFindGapCommand.subscribe(new Action1<TweetPageResponse>() {
    // public void call(TweetPageResponse pTweetPageResponse) {
    // if (pTweetPageResponse != null) {
    // // TweetPage lPage = pTweetPageResponse.tweetPage();
    // // mTimeRange = TimeRange.append(mTimeRange, lPage.tweets());
    // // mTweets.insert(lPage);
    // Log.e("", "test1");
    // } else {
    // Log.e("", "test2");
    // }
    // }
    // });
    //
    // mSubcriptions.add(RxUI.fromClick(this).map(timegap()).subscribe(mFindGapCommand));
    // mSubcriptions.add(mFindGapCommand.isRunning().subscribe(RxUI.toActivated(this)));
    // mSubcriptions.add(mFindGapCommand.isRunning().map(RxUI.not()).subscribe(RxUI.toEnabled(this)));
    // }

    public void setContent(TimeGap pTimeGap) {
        // mTimeGap = pTimeGap;

        mUINewsCreatedAt.setText(pTimeGap.earliestBound() + "==\n" + pTimeGap.oldestBound());
        // mFindGapCommand.reset();
    }

    // protected AsyncCommand<TimeGap, TweetPageResponse> createFindGapCommand() {
    // return AsyncCommand.create(new Func1<TimeGap, Observable<TweetPageResponse>>() {
    // public Observable<TweetPageResponse> call(final TimeGap pTimeGap) {
    // return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
    // public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
    // AndroidScheduler.threadPoolForIO().schedule(new Action0() {
    // public void call() {
    // try {
    // Thread.sleep(5000);
    // pObserver.onNext(null);
    // pObserver.onCompleted();
    // } catch (InterruptedException e) {
    // }
    // }
    // });
    // return Subscriptions.empty();
    // }
    // });
    // }
    // });
    // }
    //
    // protected Func1<Void, TimeGap> timegap() {
    // return new Func1<Void, TimeGap>() {
    // public TimeGap call(Void pVoid) {
    // return mTimeGap;
    // }
    // };
    // }
    //
    // protected Property<Boolean> loadingProperty() {
    // return Property.create(new PropertyAccess<Boolean>() {
    // public Boolean get() {
    // return mTimeGap.isLoading();
    // }
    //
    // public void set(Boolean pValue) {
    // mTimeGap.setLoading(pValue);
    // }
    // });
    // }
}
