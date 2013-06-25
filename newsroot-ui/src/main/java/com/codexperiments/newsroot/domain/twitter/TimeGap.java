package com.codexperiments.newsroot.domain.twitter;

import java.util.List;

import com.j256.ormlite.field.DatabaseField;

public class TimeGap
{
    @DatabaseField(generatedId = true, columnName = "TMG_ID", canBeNull = false)
    private long mId;
    @DatabaseField(columnName = "TMG_TWT_EARLIEST_ID")
    private long mEarliestBound;
    @DatabaseField(columnName = "TMG_TWT_OLDEST_ID")
    private long mOldestBound;

    // private transient List<Tweet> mTweets;

    public static TimeGap initialTimeGap()
    {
        return new TimeGap(-1, -1);
    }

    public static TimeGap futureTimeGap(List<Tweet> pTweets)
    {
        return new TimeGap(-1, pTweets.get(0).getId());
    }

    public static TimeGap pastTimeGap(List<Tweet> pTweets)
    {
        return new TimeGap(pTweets.get(pTweets.size() - 1).getId(), -1);
    }

    public TimeGap()
    {
        super();
        // mId = 0;
        mEarliestBound = -1;
        mOldestBound = -1;
        // mTweets = null;
    }

    public TimeGap(long pEarliestBound, long pOldestBound)
    {
        super();
        // mId = 0;
        mEarliestBound = pEarliestBound;
        mOldestBound = pOldestBound;
        // mTweets = null;
    }

    protected TimeGap(long pId, long pEarliestBound, long pOldestBound)
    {
        super();
        mId = pId;
        mEarliestBound = pEarliestBound;
        mOldestBound = pOldestBound;
        // mTweets = null;
    }

    public TimeGap substract(List<Tweet> pTweets, int pPageSize)
    {
        if (pTweets.size() == pPageSize) {
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

    public long getId()
    {
        return mId;
    }

    public boolean isInitialGap()
    {
        return isFutureGap() && isPastGap();
    }

    public boolean isFutureGap()
    {
        return mEarliestBound == -1;
    }

    public boolean isPastGap()
    {
        return mOldestBound == -1;
    }

    public long getEarliestBound()
    {
        return mEarliestBound;
    }

    public long getOldestBound()
    {
        return mOldestBound;
    }
}
