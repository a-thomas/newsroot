package com.codexperiments.newsroot.repository.twitter;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TweetPage;

public class TweetPageResponse {
    private TweetPage mTweetPage;
    private final TimeGap mInitialGap;
    private final TimeGap mRemainingGap;

    public TweetPageResponse(TweetPage pTweetPage, TimeGap pInitialTimeGap) {
        super();
        mTweetPage = pTweetPage;
        mInitialGap = pInitialTimeGap;
        mRemainingGap = mTweetPage.isFull() ? mInitialGap.remainingGap(mTweetPage.timeRange()) : null;
    }

    public TimeGap initialGap() {
        return mInitialGap;
    }

    public TimeGap remainingGap() {
        return mRemainingGap;
    }

    public TweetPage tweetPage() {
        return mTweetPage;
    }
}
