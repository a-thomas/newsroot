package com.codexperiments.newsroot.repository.tweet;

import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.TweetPage;

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
