package com.codexperiments.newsroot.domain.tweet;

// TODO Immutable
public class TimeGaps {
    private TimeGap mEarliestGap;
    private TimeGap mOldestGap;

    // private List<TimeGap> mTimeGaps;

    public TimeGaps() {
        super();
        mEarliestGap = null;
        mOldestGap = null;
        // mTimeGaps = Lists.newArrayList();
    }

    public TimeGaps(TimeGap pEarliestGap, TimeGap pOldestGap) {
        super();
        mEarliestGap = pEarliestGap;
        mOldestGap = pOldestGap;
    }

    public void difference(TimeRange pTimeRange) {
        // Empty time gap.
        if (mOldestGap == null || mEarliestGap == null) {
            mEarliestGap = TimeGap.futureTimeGap(pTimeRange);
            mOldestGap = TimeGap.pastTimeGap(pTimeRange);
        }
        // New range in the future.
        else if (mEarliestGap.lowerThan(pTimeRange)) {
            mEarliestGap = TimeGap.futureTimeGap(pTimeRange);
        }
        // New range in the past.
        else if (mOldestGap.greaterThan(pTimeRange)) {
            mOldestGap = TimeGap.pastTimeGap(pTimeRange);
        }
    }

    public void union(TimeGap pTimeGap) {
        // TODO Auto-generated method stub

    }
}
