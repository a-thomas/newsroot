package com.codexperiments.newsroot.domain.twitter;

public class Timeline {
    private long mId;

    private String mUsername;
    private long mEarliestId;
    private long mOldestId;
    private boolean mHasMore;

    // private List<News> mItems;

    public Timeline(String pUsername) {
        super();
        mId = -1;
        mUsername = pUsername;
        mEarliestId = -1;
        mOldestId = -1;
        mHasMore = true;
        // mItems = new ArrayList<News>();
    }

    public Timeline(long pId, long pEarliestId, long pOldestId) {
        super();
        mId = pId;
        mEarliestId = pEarliestId;
        mOldestId = pOldestId;
        mHasMore = true;
        // mItems = new ArrayList<News>();
    }

    public long getId() {
        return mId;
    }

    public String getUsername() {
        return mUsername;
    }

    public long getEarliestBound() {
        return mEarliestId;
    }

    public long getOldestBound() {
        return mOldestId;
    }

    public boolean hasMore() {
        return mHasMore;
    }

    // public List<News> getItems() {
    // return mItems;
    // }
    //
    // public void appendNewItems(List<News> pItems) {
    // mItems.addAll(0, pItems);
    // }
    //
    // public void appendOldItems(List<News> pItems) {
    // mItems.addAll(pItems);
    // }

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
    // public long getEarliestBound() {
    // if (mItems.size() == 0) {
    // return -1;
    // } else {
    // return mItems.get(0).getTimelineId();
    // }
    // }
    //
    // public long getOldestBound() {
    // if (mItems.size() == 0) {
    // return -1;
    // } else {
    // return mItems.get(mItems.size() - 1).getTimelineId();
    // }
    // }
    //
    // public boolean hasOldestBound() {
    // return getEarliestBound() != -1;
    // }
    //
    // public boolean hasEarliestBound() {
    // return getEarliestBound() != -1;
    // }
}
