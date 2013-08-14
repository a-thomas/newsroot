package com.codexperiments.newsroot.domain.twitter;

import java.util.List;

// TODO Make immutable
public class TimeGap implements News {
    private long mId;
    private long mEarliestBound;
    private long mOldestBound;

    // private transient List<Tweet> mTweets;

    public static TimeGap initialTimeGap() {
        return new TimeGap(-1, -1);
    }

    public static TimeGap futureTimeGap(List<Tweet> pTweets) {
        return new TimeGap(-1, pTweets.get(0).getId());
    }

    public static TimeGap pastTimeGap(List<Tweet> pTweets) {
        return new TimeGap(pTweets.get(pTweets.size() - 1).getId(), -1);
    }

    public TimeGap() {
        super();
        // mId = 0;
        mEarliestBound = -1;
        mOldestBound = -1;
        // mTweets = null;
    }

    public TimeGap(long pEarliestBound, long pOldestBound) {
        super();
        // mId = 0;
        mEarliestBound = pEarliestBound;
        mOldestBound = pOldestBound;
        // mTweets = null;
    }

    protected TimeGap(long pId, long pEarliestBound, long pOldestBound) {
        super();
        mId = pId;
        mEarliestBound = pEarliestBound;
        mOldestBound = pOldestBound;
        // mTweets = null;
    }

    public TimeGap substract(List<Tweet> pTweets, int pPageSize) {
        if (pTweets.size() >= pPageSize) {
            if (isFutureGap()) {
                long lEarliestTweetId = pTweets.get(0).getId();
                return new TimeGap(mEarliestBound, (lEarliestTweetId > mOldestBound) ? lEarliestTweetId : mOldestBound);
            } else {
                long lOldestTweetId = pTweets.get(pTweets.size() - 1).getId();
                if ((lOldestTweetId < mEarliestBound) && (lOldestTweetId > mOldestBound)) {
                    return new TimeGap((lOldestTweetId < mEarliestBound) ? lOldestTweetId : mEarliestBound, mOldestBound);
                } else {
                    return this;
                }
            }
        } else {
            return null;
        }
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        mId = pId;
    }

    @Override
    public long getTimelineId() {
        return mOldestBound;
    }

    public void setEarliestBound(long pEarliestBound) {
        mEarliestBound = pEarliestBound;
    }

    public void setOldestBound(long pOldestBound) {
        mOldestBound = pOldestBound;
    }

    public boolean isInitialGap() {
        return isFutureGap() && isPastGap();
    }

    public boolean isFutureGap() {
        return mEarliestBound == -1;
    }

    public boolean isPastGap() {
        return mOldestBound == -1;
    }

    public long getEarliestBound() {
        return mEarliestBound;
    }

    public long getOldestBound() {
        return mOldestBound;
    }
}
