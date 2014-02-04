package com.codexperiments.newsroot.repository.tweet;

import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.TweetPage;

public class TweetPageResponse {
    private TweetPage mTweetPage;

    private final TimeGap mInitialGap;
    private final TimeGap mRemainingGap;

    public static final TweetPageResponse emptyResponse(TimeGap pInitialTimeGap, int pPageSize) {
        return new TweetPageResponse(new TweetPage(new TweetDTO[] {}, pPageSize), pInitialTimeGap);
    }

    public TweetPageResponse(TweetPage pTweetPage, TimeGap pInitialTimeGap) {
        super();
        mTweetPage = pTweetPage;
        mInitialGap = pInitialTimeGap;
        mRemainingGap = mTweetPage.isFull() ? mInitialGap.remainingGap(mTweetPage.timeRange()) : null;
    }

    // public TimeGap initialGap() {
    // return mInitialGap;
    // }
    //
    // public TimeGap remainingGap() {
    // return mRemainingGap;
    // }

    public TweetPage tweetPage() {
        return mTweetPage;
    }
}
