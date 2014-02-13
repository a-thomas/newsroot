package com.codexperiments.newsroot.domain.tweet;

import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.data.tweet.TweetDTO;

public class TweetPage implements Page<TweetDTO>/* , Iterable<TweetDTO> */{
    private final TweetDTO[] mTweets;
    private final TimeRange mTimeRange;
    private final boolean mIsFull;

    public TweetPage(TweetDTO[] pTweets, int pPageSize) {
        super();
        mTweets = pTweets;
        mIsFull = pTweets.length >= pPageSize;
        mTimeRange = TimeRange.from(pTweets);
    }

    public TimeRange timeRange() {
        return mTimeRange;
    }

    public TweetDTO[] tweets() {
        return mTweets;
    }

    public boolean isEmpty() {
        return mTweets.length <= 0;
    }

    public boolean isFull() {
        return mIsFull;
    }

    @Override
    public long oldestBound() {
        return mTimeRange.oldestBound();
    }

    @Override
    public long earliestBound() {
        return mTimeRange.earliestBound();
    }

    @Override
    public TweetDTO get(int pIndex) {
        return mTweets[pIndex];
    }

    @Override
    public int size() {
        return mTweets.length;
    }

    // @Override
    // public Iterator<TweetDTO> iterator() {
    // return mTweets.iterator();
    // }

    // @Override
    // public Iterator<Tweet> iterator() {
    // return mTweets.iterator();
    // }
    //
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
