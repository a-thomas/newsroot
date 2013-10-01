package com.codexperiments.newsroot.repository.twitter;

import rx.Observable;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;

public interface TwitterRepository {
    static final int DEFAULT_PAGE_COUNT = 5;
    static final int DEFAULT_PAGE_SIZE = 20; // TODO

    Timeline findTimeline(String pUsername);

    Observable<TweetPageResponse> findTweets(Timeline pTimeline, TimeGap pTimeGap, int pPageCount, int pPageSize);
}
