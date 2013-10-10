package com.codexperiments.newsroot.domain.twitter;

import java.util.List;

public class TimeRange {
    private static final TimeRange EMPTY_TIMERANGE = new TimeRange();

    private long mEarliestBound;
    private long mOldestBound;

    public static TimeRange from(List<Tweet> pTweets) {
        if (pTweets.size() > 0) {
            return new TimeRange(pTweets.get(0).getId(), pTweets.get(pTweets.size() - 1).getId());
        } else {
            return EMPTY_TIMERANGE;
        }
    }

    public static TimeRange append(TimeRange pTimeRange, List<Tweet> pTweets) {
        int lSize = pTweets.size();
        if (lSize <= 0) {
            return pTimeRange;
        } else {
            long lEarliestBound = pTweets.get(0).getId();
            long lOldestBound = pTweets.get(lSize - 1).getId();
            if (pTimeRange != null) {
                if (lEarliestBound < pTimeRange.mEarliestBound) lEarliestBound = pTimeRange.mEarliestBound;
                if (lOldestBound > pTimeRange.mOldestBound) lOldestBound = pTimeRange.mOldestBound;
            }
            return new TimeRange(lEarliestBound, lOldestBound);
        }
    }

    private TimeRange() {
        super();
        mEarliestBound = Long.MAX_VALUE;
        mOldestBound = Long.MIN_VALUE;
    }

    public TimeRange(long pEarliestBound, long pOldestBound) {
        super();
        mEarliestBound = pEarliestBound;
        mOldestBound = pOldestBound;
    }

    public long earliestBound() {
        return mEarliestBound;
    }

    public long oldestBound() {
        return mOldestBound;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TimeRange other = (TimeRange) obj;
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
        return "TimeRange [mEarliestBound=" + mEarliestBound + ", mOldestBound=" + mOldestBound + "]";
    }

    public static TimeRange union(TimeRange pTimeRange1, TimeRange pTimeRange2) {
        if (pTimeRange1 == null) return pTimeRange2;
        else if (pTimeRange2 == null) return pTimeRange1;
        else {
            long lRange1EarliestBound = pTimeRange1.mEarliestBound, lRange2EarliestBound = pTimeRange2.mEarliestBound;
            long lRange1OldestBound = pTimeRange1.mOldestBound, lRange2OldestBound = pTimeRange2.mOldestBound;

            return new TimeRange(lRange2EarliestBound > lRange1EarliestBound ? lRange2EarliestBound : lRange1EarliestBound,
                                 lRange2OldestBound < lRange1OldestBound ? lRange2OldestBound : lRange1OldestBound);
        }
    }
}
