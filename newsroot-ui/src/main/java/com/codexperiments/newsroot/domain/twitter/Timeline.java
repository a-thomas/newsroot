package com.codexperiments.newsroot.domain.twitter;

public class Timeline {
    private long mId;

    private String mUsername;

    private TimeRange mTimeRange;

    // private long mEarliestId;
    // private long mOldestId;

    // private boolean mHasMore;

    public Timeline(String pUsername) {
        super();
        mId = -1;
        mUsername = pUsername;
        mTimeRange = null;
        // mEarliestId = -1;
        // mOldestId = -1;
    }

    // public Timeline(long pId, long pEarliestId, long pOldestId) {
    // super();
    // mId = pId;
    // mTimeRange = new TimeRange(pEarliestId, pOldestId);
    // // mEarliestId = pEarliestId;
    // // mOldestId = pOldestId;
    // }

    public long id() {
        return mId;
    }

    public TimeGap futureGap() {
        return TimeGap.futureTimeGap(mTimeRange);
    }

    public TimeGap pastGap() {
        return TimeGap.pastTimeGap(mTimeRange);
    }

    // public String username() {
    // return mUsername;
    // }
    //
    // public long earliestBound() {
    // return mEarliestId;
    // }
    //
    // public long oldestBound() {
    // return mOldestId;
    // }
    //
    // public boolean hasMore() {
    // return mHasMore;
    // }
    //
    // public TimeGap futureGap() {
    // return new TimeGap(-1, mEarliestId);
    // }
    //
    // public TimeGap pastGap() {
    // return new TimeGap(mOldestId, -1);
    // }
    //
    // public void addTweets(TweetPage pTweetPage) {
    // TimeGap lRemainingGap = pTweetPage.remainingGap();
    // mOldestId = lRemainingGap.isPastGap() ? lRemainingGap.getEarliestBound() : mOldestId;
    // mEarliestId = lRemainingGap.isFutureGap() ? lRemainingGap.getOldestBound() : mEarliestId;
    // }
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
