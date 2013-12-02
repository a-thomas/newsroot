package com.codexperiments.newsroot.data.tweet;

public class TimeGapDTO {
    private long mId;
    private long mEarliestBound;
    private long mOldestBound;
    private boolean mLoading;

    public TimeGapDTO() {
        super();
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        mId = pId;
    }

    public long getEarliestBound() {
        return mEarliestBound;
    }

    public void setEarliestBound(long pEarliestBound) {
        mEarliestBound = pEarliestBound;
    }

    public long getOldestBound() {
        return mOldestBound;
    }

    public void setOldestBound(long pOldestBound) {
        mOldestBound = pOldestBound;
    }

    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean pLoading) {
        mLoading = pLoading;
    }
}
