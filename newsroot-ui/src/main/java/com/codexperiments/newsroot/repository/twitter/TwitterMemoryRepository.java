package com.codexperiments.newsroot.repository.twitter;

import java.util.Map;

import rx.Observable;
import android.app.Application;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.google.common.collect.Maps;

public class TwitterMemoryRepository implements TwitterRepository {
    private TwitterRepository mWrappedRepository;
    private Map<String, CacheEntry> mTimelineCache;

    public TwitterMemoryRepository(Application pApplication, TwitterRepository pRepository) {
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
