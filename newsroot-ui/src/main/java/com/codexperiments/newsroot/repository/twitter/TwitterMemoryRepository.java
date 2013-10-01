// package com.codexperiments.newsroot.repository.twitter;
//
// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Comparator;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
//
// import rx.Observable;
// import rx.Observable.OnSubscribeFunc;
// import rx.Observer;
// import rx.Subscription;
// import android.app.Application;
//
// import com.codexperiments.newsroot.domain.twitter.News;
// import com.codexperiments.newsroot.domain.twitter.TimeGap;
// import com.codexperiments.newsroot.domain.twitter.TimeRange;
// import com.codexperiments.newsroot.domain.twitter.Timeline;
// import com.codexperiments.newsroot.domain.twitter.Tweet;
// import com.codexperiments.newsroot.domain.twitter.TweetPage;
// import com.google.common.collect.Maps;
//
// public class TwitterMemoryRepository implements TwitterRepository {
// private TwitterRepository mRepository;
// private Map<Timeline, CacheEntry> mTimelines;
// private Map<String, Timeline> mFollowing;
//
// public TwitterMemoryRepository(Application pApplication, TwitterRepository pRepository) {
// super();
// mTimelines = Maps.newHashMapWithExpectedSize(64);
// mFollowing = new HashMap<String, Timeline>();
// }
//
// @Override
// public Timeline findTimeline(String pUsername) {
// Timeline lTimeline = mFollowing.get(pUsername);
// if (lTimeline != null) {
// lTimeline = new Timeline(pUsername);
// }
// return lTimeline;
// }
//
// @Override
// public Observable<TweetPage> findTweets(Timeline pTimeline, TimeGap pTimeGap, int pPageCount, int pPageSize) {
// CacheEntry lCacheEntry = mTimelines.get(pTimeline);
// if (lCacheEntry == null) {
// lCacheEntry = new CacheEntry(pTimeline);
// mTimelines.put(pTimeline, lCacheEntry);
// }
//
// if (pTimeGap.isInitialGap() && !lCacheEntry.isEmpty()) {
// return lCacheEntry.findCachedTweets(pTimeGap);
// } else {
// return lCacheEntry.cacheTweetPages(mRepository.findTweets(pTimeline, pTimeGap, pPageCount, pPageSize));
// }
// }
//
// private static class CacheEntry {
// private TimeRange mTimeRange;
// private List<Tweet> mTweets;
// private boolean mEmpty;
//
// public CacheEntry(Timeline pTimeline) {
// super();
// mTimeRange = null;
// mTweets = new ArrayList<Tweet>();
// mEmpty = true;
// }
//
// public boolean isEmpty() {
// return mEmpty;
// }
//
// public Observable<TweetPage> findCachedTweets(TimeGap pTimeGap) {
// return Observable.just(new TweetPage(mTweets, pTimeGap, mTweets.size()));
// // if (pTimeGap.greaterThan(mTimeRange) || pTimeGap.lowerThan(mTimeRange) || (mTweets.contains(pTimeGap))) {
// // return null;
// // } else {
// // int lStartIndex = Collections.binarySearch(mTweets, null, null);
// // int lEndIndex = Collections.binarySearch(mTweets, null, null);
// // return mTweets.subList(lStartIndex, lEndIndex);
// // }
// }
//
// private Observable<TweetPageResponse> cacheTweetPages(final Observable<TweetPageResponse> pTweetPages) {
// return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
// public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
// return pTweetPages.subscribe(new Observer<TweetPageResponse>() {
// public void onNext(TweetPageResponse pTweetPageResponse) {
// mEmpty = false;
//
// TimeGap lTimeGap = pTweetPageResponse.initialGap();
// TimeGap lRemainingGap = pTweetPageResponse.remainingGap();
// // Inner gap.
// if (lTimeGap.isInnerGap()) {
// // Find the gap in the list.
// int lGapIndex = Collections.binarySearch(mTweets, lTimeGap, new Comparator<News>() {
// public int compare(News pNews1, News pNews2) {
// long lNews1Id = pNews1.getTimelineId(), lNews2Id = pNews2.getTimelineId();
// if (lNews1Id > lNews2Id) return 1;
// else if (lNews1Id < lNews2Id) return -1;
// else return 0;
// }
// });
//
// // Replace the old gap with the newly found tweets.
// if (lGapIndex != -1 && lTimeGap.equals(mTweets.get(lGapIndex))) {
// mTweets.remove(lGapIndex);
// // if (lRemainingGap != null) mTweets.add(lGapIndex, lRemainingGap);
// mTweets.addAll(lGapIndex + 1, pTweetPageResponse.tweetPage().tweets());
// }
// }
// // Past or future gap.
// else {
// mTimeRange = TimeRange.union(mTimeRange, pTweetPage.timeRange());
// if (lTimeGap.isFutureGap()) {
// // if (lRemainingGap != null) mTweets.add(lRemainingGap);
// mTweets.addAll(pTweetPageResponse.tweetPage().tweets());
// } else if (lTimeGap.isPastGap()) {
// mTweets.addAll(0, pTweetPageResponse.tweetPage().tweets());
// }
// }
//
// pObserver.onNext(pTweetPage);
// }
//
// public void onCompleted() {
// pObserver.onCompleted();
// }
//
// public void onError(Throwable pThrowable) {
// pObserver.onError(pThrowable);
// }
// });
// }
// });
// }
// }
// }
