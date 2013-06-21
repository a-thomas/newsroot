package com.codexperiments.newsroot.domain.twitter;

import java.util.List;

public class Page
{
    public static final int DEFAULT_PAGE_SIZE = 20;

    private int mSliceCount;
    private long mEarliestId;
    private long mOldestId;

    public Page()
    {
        super();
        mSliceCount = DEFAULT_PAGE_SIZE;
        mEarliestId = -1;
        mOldestId = -1;
    }

    public Page(int pCount)
    {
        super();
        mSliceCount = pCount;
    }

    public void refreshFrom(List<Tweet> pTweets)
    {
        if (pTweets.size() > 0) {
            Tweet lFirstTweet = pTweets.get(0);
            mEarliestId = lFirstTweet.getId();
            Tweet lLastTweet = pTweets.get(pTweets.size() - 1);
            mOldestId = lLastTweet.getId();
        } else {
            mEarliestId = -1;
            mOldestId = -1;
        }
    }

    public int getSliceCount()
    {
        return mSliceCount;
    }

    public boolean hasTweets()
    {
        return (mEarliestId != -1) && (mOldestId != -1);
    }

    public long getEarliestId()
    {
        return mEarliestId;
    }

    public long getOldestId()
    {
        return mOldestId;
    }
}
