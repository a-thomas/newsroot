package com.codexperiments.newsroot.ui.fragment.newslist;

import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.News;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.TimeRange;
import com.codexperiments.newsroot.domain.tweet.TweetPage;

public class NewsPage implements Page<News> {
    private final News[] mNews;
    private final TimeRange mTimeRange;

    public NewsPage(TimeGap pTimeGap) {
        super();
        mNews = new News[] { pTimeGap };
        mTimeRange = new TimeRange(pTimeGap.earliestBound(), pTimeGap.oldestBound());
    }

    public NewsPage(TweetPage pTweetPage) {
        super();
        TweetDTO[] lTweets = pTweetPage.tweets();
        mNews = lTweets;
        mTimeRange = pTweetPage.timeRange();
    }

    public TimeRange timeRange() {
        return mTimeRange;
    }

    public News[] news() {
        return mNews;
    }

    @Override
    public long lowerBound() {
        return mTimeRange.oldestBound();
    }

    @Override
    public long upperBound() {
        return mTimeRange.earliestBound();
    }

    @Override
    public News get(int pIndex) {
        return mNews[pIndex];
    }

    @Override
    public int size() {
        return mNews.length;
    }
}
