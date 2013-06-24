package com.codexperiments.newsroot.domain.twitter;

import com.j256.ormlite.field.DatabaseField;

public class Timeline
{
    @DatabaseField(id = true, columnName = "TML_ID", canBeNull = false)
    private long mId;

    // @DatabaseField(columnName = "TML_TWT_EARLIEST_ID")
    // private long mEarliestId;
    // @DatabaseField(columnName = "TML_TWT_OLDEST_ID")
    // private long mOldestId;

    public Timeline()
    {
        super();
        mId = -1;
        // mEarliestId = -1;
        // mOldestId = -1;
    }

    // public TimeGap refresh(TimeGap pTimeGap, List<Tweet> pTweets, int pPageSize)
    // {
    // TimeGap lTimeGap = null;
    // int lEffectivePageSize = pTweets.size();
    // if (lEffectivePageSize > 0) {
    // long lEarliestTweetId = pTweets.get(0).getId();
    // long lOldestTweetId = pTweets.get(pTweets.size() - 1).getId();
    //
    // if (lEffectivePageSize == pPageSize) {
    // lTimeGap = new TimeGap(pId, mEarliestId, lOldestTweetId);
    // }
    // if (lEarliestTweetId > mEarliestId) {
    // mEarliestId = lEarliestTweetId;
    // }
    // if (lOldestTweetId < mEarliestId) {
    // mOldestId = lOldestTweetId;
    // }
    // }
    // return lTimeGap;
    // }
    //
    // public boolean hasTweets()
    // {
    // return (mEarliestId != -1) && (mOldestId != -1);
    // }
    //
    // public long getEarliestId()
    // {
    // return mEarliestId;
    // }
    //
    // public long getOldestId()
    // {
    // return mOldestId;
    // }
}
