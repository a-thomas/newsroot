package com.codexperiments.newsroot.domain.twitter;

import java.util.Iterator;
import java.util.List;

import rx.Observable;
import rx.Observer;

public class TweetPage implements Iterable<Tweet> {
    private final List<Tweet> mTweets;
    private final TimeGap mTimeGap;
    private final TimeGap mRemainingGap;

    public TweetPage(List<Tweet> pTweets, TimeGap pTimeGap, int pPageSize) {
        super();
        mTweets = pTweets;
        mTimeGap = pTimeGap;
        mRemainingGap = mTimeGap.substract(mTweets, pPageSize);
    }

    public TimeGap timeGap() {
        return mTimeGap;
    }

    public TimeGap remainingGap() {
        return mRemainingGap;
    }

    public List<Tweet> tweets() {
        return mTweets;
    }

    @Override
    public Iterator<Tweet> iterator() {
        return mTweets.iterator();
    }

    public TweetPage apply(final Observable<Tweet> pObservable) {
        pObservable.subscribe(new Observer<Tweet>() {
            public void onNext(Tweet pTweet) {
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
            }
        });
        return this;
    }
}
