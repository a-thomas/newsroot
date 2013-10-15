package com.codexperiments.newsroot.ui.fragment;

import java.util.List;

import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TimeRange;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.presentation.NewsPresentation;
import com.google.common.collect.Lists;

public class NewsPage implements Page<NewsPresentation> {
    private final List<NewsPresentation> mNews;
    private final TimeRange mTimeRange;

    public NewsPage(TimeGap pTimeGap) {
        super();
        mNews = Lists.<NewsPresentation> newArrayList(new NewsTimeGapItem.Model(pTimeGap));
        mTimeRange = new TimeRange(pTimeGap.earliestBound(), pTimeGap.oldestBound());
    }

    public NewsPage(TweetPage pTweetPage) {
        super();
        List<Tweet> lTweets = pTweetPage.tweets();
        mNews = Lists.newArrayListWithCapacity(lTweets.size());
        for (Tweet lTweet : lTweets) {
            mNews.add(new NewsItem.Model(lTweet));
        }
        mTimeRange = pTweetPage.timeRange();
    }

    public TimeRange timeRange() {
        return mTimeRange;
    }

    public List<? extends NewsPresentation> news() {
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
    public NewsPresentation get(int pIndex) {
        return mNews.get(pIndex);
    }

    @Override
    public int size() {
        return mNews.size();
    }
}
