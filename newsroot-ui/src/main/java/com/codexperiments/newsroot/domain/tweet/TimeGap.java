package com.codexperiments.newsroot.domain.tweet;

// TODO Make immutable
public class TimeGap implements News {
    private/* final */long mId;
    private/* final */long mEarliestBound;
    private/* final */long mOldestBound;

    private boolean mLoading;

    // private transient List<Tweet> mTweets;

    public static TimeGap initialTimeGap() {
        return new TimeGap();
    }

    // public static TimeGap timeGap(List<Tweet> pTweets) {
    // return new TimeGap(pTweets.get(pTweets.size() - 1).getId(), pTweets.get(0).getId());
    // }
    public static TimeGap futureTimeGap(long pEarliestId) {
        return new TimeGap(Long.MAX_VALUE, pEarliestId);
    }

    public static TimeGap pastTimeGap(long pOldestId) {
        return new TimeGap(pOldestId, Long.MIN_VALUE);
    }

    public static TimeGap futureTimeGap(TimeRange pTimeRange) {
        return (pTimeRange != null) ? new TimeGap(Long.MAX_VALUE, pTimeRange.earliestBound()) : TimeGap.initialTimeGap();
    }

    public static TimeGap pastTimeGap(TimeRange pTimeRange) {
        return (pTimeRange != null) ? new TimeGap(pTimeRange.oldestBound() - 1, Long.MIN_VALUE) : TimeGap.initialTimeGap();
    }

    // public static TimeGap futureTimeGap(List<Tweet> pTweets) {
    // return new TimeGap(Long.MAX_VALUE, pTweets.get(0).getId());
    // }
    //
    // public static TimeGap pastTimeGap(List<Tweet> pTweets) {
    // return new TimeGap(pTweets.get(pTweets.size() - 1).getId() - 1, Long.MIN_VALUE);
    // }

    private TimeGap() {
        super();
        // mId = 0;
        mEarliestBound = Long.MAX_VALUE;
        mOldestBound = Long.MIN_VALUE;
        // mTweets = null;
    }

    public TimeGap(long pEarliestBound, long pOldestBound) {
        super();
        // mId = 0;
        mEarliestBound = pEarliestBound;
        mOldestBound = pOldestBound;
        // mTweets = null;
    }

    public TimeGap(long pId, long pEarliestBound, long pOldestBound) {
        super();
        mId = pId;
        mEarliestBound = pEarliestBound;
        mOldestBound = pOldestBound;
        // mTweets = null;
    }

    // public TimeGap add(List<Tweet> pTweets) {
    // long lEarliestBound = pTweets.get(pTweets.size() - 1).getId();
    // long lOldestBound = pTweets.get(0).getId();
    //
    // if (mEarliestBound > lEarliestBound) lEarliestBound = mEarliestBound;
    // if (mOldestBound > lOldestBound) lOldestBound = mOldestBound;
    // return new TimeGap(lEarliestBound, lOldestBound);
    // }
    //
    // public TimeGap substract(TimeRange pTimeRange) {
    // if (isPastGap()) {
    // long lOldestTweetId = pTimeRange.oldestBound();
    // if (isInitialGap()) {
    // return new TimeGap(lOldestTweetId, mOldestBound);
    // } else if ((lOldestTweetId < mEarliestBound) && (lOldestTweetId > mOldestBound)) {
    // return new TimeGap((lOldestTweetId < mEarliestBound) ? lOldestTweetId : mEarliestBound, mOldestBound);
    // } else {
    // return this;
    // }
    // } else {
    // long lEarliestTweetId = pTimeRange.earliestBound();
    // return new TimeGap(mEarliestBound, (lEarliestTweetId > mOldestBound) ? lEarliestTweetId : mOldestBound);
    // }
    // }
    public TimeGap remainingGap(TimeRange pTimeRange) {
        long lRangeEarliestBound = pTimeRange.earliestBound();
        long lRangeOldestBound = pTimeRange.oldestBound() - 1; // TODO Check bug because of -1

        if ((lRangeOldestBound < mOldestBound) || (lRangeEarliestBound > mEarliestBound)) {
            throw new IllegalArgumentException();
        }
        return (lRangeOldestBound > mOldestBound) ? new TimeGap(lRangeOldestBound, mOldestBound) : null;
    }

    public TimeGap remainingGap(TweetPage pTweetPage) {
        long lRangeEarliestBound = pTweetPage.earliestBound();
        long lRangeOldestBound = pTweetPage.oldestBound() - 1; // TODO Check bug because of -1

        if ((lRangeOldestBound < mOldestBound) || (lRangeEarliestBound > mEarliestBound)) {
            throw new IllegalArgumentException();
        }
        return (lRangeOldestBound > mOldestBound) ? new TimeGap(lRangeOldestBound, mOldestBound) : null;
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        mId = pId;
    }

    @Override
    public long getTimelineId() {
        return mOldestBound + 1;
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
        return mEarliestBound == Long.MAX_VALUE;
    }

    public boolean isPastGap() {
        return mOldestBound == Long.MIN_VALUE;
    }

    public boolean isInnerGap() {
        return (mEarliestBound != Long.MAX_VALUE) && (mOldestBound != Long.MIN_VALUE);
    }

    public long earliestBound() {
        return mEarliestBound;
    }

    public long oldestBound() {
        return mOldestBound;
    }

    public boolean greaterThan(TimeRange pTimeRange) {
        return mOldestBound > pTimeRange.earliestBound();
    }

    public boolean lowerThan(TimeRange pTimeRange) {
        return mEarliestBound < pTimeRange.oldestBound();
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean pLoading) {
        mLoading = pLoading;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TimeGap other = (TimeGap) obj;
        if (mEarliestBound != other.mEarliestBound) return false;
        if (mOldestBound != other.mOldestBound) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (mEarliestBound ^ (mEarliestBound >>> 32));
        result = prime * result + (int) (mOldestBound ^ (mOldestBound >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TimeGap [mId=" + mId + ", mEarliestBound=" + mEarliestBound + ", mOldestBound=" + mOldestBound + "]";
    }
}
