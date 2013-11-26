package com.codexperiments.newsroot.repository.tweet;

import rx.Observable;

import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;

public interface TweetRepository {
    static final int DEFAULT_PAGE_COUNT = 5;
    static final int DEFAULT_PAGE_SIZE = 20; // TODO

    Timeline findTimeline(String pUsername);

    Observable<TweetPageResponse> findTweets(Timeline pTimeline, TimeGap pTimeGap, int pPageCount, int pPageSize);
}
