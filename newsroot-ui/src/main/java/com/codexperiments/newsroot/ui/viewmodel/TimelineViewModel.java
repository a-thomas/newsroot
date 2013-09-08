package com.codexperiments.newsroot.ui.viewmodel;

import java.util.ArrayList;
import java.util.List;

import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.google.common.collect.Iterables;

public class TimelineViewModel {
    private Timeline mTimeline;
    private List<News> mItems;
    private boolean mHasMore;
    private boolean mFromCache;

    public TimelineViewModel(Timeline pTimeline) {
        super();
        mTimeline = pTimeline;
        mItems = new ArrayList<News>();
        mHasMore = true;
        mFromCache = true;
    }

    public void addTweets(TweetPage pTweetPage) {
        if (pTweetPage.timeGap().isPastGap()) {
            boolean lHasMore = false;// !pTweetPage.isLastPage();
            if (mFromCache) mFromCache = lHasMore;
            else mHasMore = lHasMore;
        }
        mTimeline.addTweets(pTweetPage);
        Iterables.addAll(mItems, pTweetPage);
    }

    public boolean hasMore() {
        return mHasMore;
    }

    public boolean fromCache() {
        return mFromCache;
    }

    public List<News> tweets() {
        return mItems;
    }
}
