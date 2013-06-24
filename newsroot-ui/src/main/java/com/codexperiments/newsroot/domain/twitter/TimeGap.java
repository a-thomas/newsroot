package com.codexperiments.newsroot.domain.twitter;

import java.util.List;

import com.j256.ormlite.field.DatabaseField;

public class TimeGap
{
    // @DatabaseField(columnName = "TMG_ID", canBeNull = false)
    // private long mId;
    @DatabaseField(columnName = "TMG_TWT_START_ID")
    private long mEarliestId;
    @DatabaseField(columnName = "TMG_TWT_END_ID")
    private long mOldestId;
    private transient List<Tweet> mTweets;

    public TimeGap()
    {
        super();
        // mId = 0;
        mEarliestId = -1;
        mOldestId = -1;
        mTweets = null;
    }

    public TimeGap(long pEarliestId, long pOldestId)
    {
        super();
        // mId = 0;
        mEarliestId = pEarliestId;
        mOldestId = pOldestId;
        mTweets = null;
    }

    public TimeGap substract(List<Tweet> pTweets, int pPageSize)
    {
        if (pTweets.size() == pPageSize) {
            // long lEarliestTweetId = pTweets.get(0).getId();
            long lEarliestTweetId = pTweets.get(pTweets.size() - 1).getId();
            mOldestId = lEarliestTweetId;
            return new TimeGap(lEarliestTweetId, mOldestId);
        } else {
            return null;
        }
    }

    public boolean hasEarliestBound()
    {
        return mEarliestId > -1;
    }

    public long getEarliestId()
    {
        return mEarliestId;
    }

    public boolean hasOldestBound()
    {
        return mOldestId > -1;
    }

    public long getOldestId()
    {
        return mOldestId;
    }
}
