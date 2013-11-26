package com.codexperiments.newsroot.repository.tweet;

import java.util.Map;

import rx.Observable;
import android.app.Application;

import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.google.common.collect.Maps;

public class TweetMemoryRepository implements TweetRepository {
    private TweetRepository mWrappedRepository;
    private Map<String, CacheEntry> mTimelineCache;

    public TweetMemoryRepository(Application pApplication, TweetRepository pRepository) {
        super();
        mTimelineCache = Maps.newHashMapWithExpectedSize(64);
    }

    @Override
    public Timeline findTimeline(String pUsername) {
        Timeline lTimeline;
        CacheEntry lCacheEntry = mTimelineCache.get(pUsername);
        if (lCacheEntry != null) {
            lTimeline = lCacheEntry.mTimeline;
        } else {
            lTimeline = mWrappedRepository.findTimeline(pUsername);
            mTimelineCache.put(pUsername, new CacheEntry(lTimeline));
        }
        return lTimeline;
    }

    @Override
    public Observable<TweetPageResponse> findTweets(Timeline pTimeline, TimeGap pTimeGap, int pPageCount, int pPageSize) {
        return mWrappedRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize);
    }

    private static class CacheEntry {
        private Timeline mTimeline;

        public CacheEntry(Timeline pTimeline) {
            super();
            mTimeline = pTimeline;
        }
    }
}
