package com.codexperiments.newsroot.domain.twitter;

import java.util.ArrayList;
import java.util.List;

public class Timeline {
    private long mId;
    private long mEarliestId;
    private long mOldestId;
    private List<News> mItems;

    public Timeline() {
        super();
        mId = -1;
        mEarliestId = -1;
        mOldestId = -1;
        mItems = new ArrayList<News>();
    }

    public Timeline(long pId, long pEarliestId, long pOldestId) {
        super();
        mId = pId;
        mEarliestId = pEarliestId;
        mOldestId = pOldestId;
        mItems = new ArrayList<News>();
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        mId = pId;
    }

    public List<News> getItems() {
        return mItems;
    }

    public void appendNewItems(List<News> pItems) {
        mItems.addAll(0, pItems);
    }

    public void appendOldItems(List<News> pItems) {
        mItems.addAll(pItems);
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
    public long getEarliestBound() {
        if (mItems.size() == 0) {
            return -1;
        } else {
            return mItems.get(0).getTimelineId();
        }
    }

    public long getOldestBound() {
        if (mItems.size() == 0) {
            return -1;
        } else {
            return mItems.get(mItems.size() - 1).getTimelineId();
        }
    }

    public boolean hasOldestBound() {
        return getEarliestBound() != -1;
    }

    public boolean hasEarliestBound() {
        return getEarliestBound() != -1;
    }
}
