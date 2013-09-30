package com.codexperiments.newsroot.domain.twitter;

import java.util.Iterator;
import java.util.List;

import com.codexperiments.newsroot.common.Page;

public class TweetPage implements Page<Tweet> {
    private final List<Tweet> mTweets;

    private final TimeRange mTimeRange;
    private final TimeGap mInitialGap;
    private final TimeGap mRemainingGap;
    private final boolean mIsFull;

    public TweetPage(List<Tweet> pTweets, TimeGap pTimeGap, int pPageSize) {
        super();
        mTweets = pTweets;
        mIsFull = pTweets.size() >= pPageSize;

        mTimeRange = TimeRange.from(pTweets);
        mInitialGap = pTimeGap;
        mRemainingGap = mIsFull ? mInitialGap.remainingGap(mTimeRange) : null;
    }

    public TimeGap timeGap() {
        return mInitialGap;
    }

    public TimeRange timeRange() {
        return mTimeRange;
    }

    public TimeGap remainingGap() {
        return mRemainingGap;
    }

    public List<Tweet> tweets() {
        return mTweets;
    }

    @Override
    public long lowerBound() {
        return mTimeRange.oldestBound();
    }

    @Override
    public long upperBound() {
        return mTimeRange.earliestBound();
    }

    @Override
    public Tweet get(int pIndex) {
        return mTweets.get(pIndex);
    }

    @Override
    public int size() {
        return mTweets.size();
    }

    @Override
    public Iterator<Tweet> iterator() {
        return mTweets.iterator();
    }

    // public <T> void apply(final Observer<? super TweetPage> pPagedObserver, final Observable<T> pObservable) {
    // pObservable.subscribe(new Observer<T>() {
    // public void onNext(T pT) {
    // }
    //
    // public void onCompleted() {
    // pPagedObserver.onNext(TweetPage.this);
    // }
    //
    // public void onError(Throwable pThrowable) {
    // pPagedObserver.onError(pThrowable);
    // }
    // });
    // }
}
