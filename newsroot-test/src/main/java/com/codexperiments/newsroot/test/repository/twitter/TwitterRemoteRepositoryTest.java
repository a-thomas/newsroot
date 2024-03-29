package com.codexperiments.newsroot.test.repository.twitter;

import static com.codexperiments.newsroot.test.common.TestRx.subscribeAndWait;
import static com.codexperiments.newsroot.test.server.MockBackendMatchers.hasQueryParam;
import static com.codexperiments.newsroot.test.server.MockBackendMatchers.hasUrl;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.simpleframework.http.Request;

import rx.Observer;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.repository.twitter.TwitterQuery;
import com.codexperiments.newsroot.repository.twitter.TwitterRemoteRepository;
import com.codexperiments.newsroot.test.common.BackendTestCase;
import com.codexperiments.newsroot.test.data.TweetPageData;
import com.codexperiments.newsroot.test.data.TwitterManagerTestConfig;

public class TwitterRemoteRepositoryTest extends BackendTestCase {
    private TwitterManager mTwitterManager;
    private TwitterRemoteRepository mTwitterRepository;
    private Observer<TweetPageResponse> mTweetPageObserver;

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        mTweetPageObserver = mock(Observer.class, withSettings().verboseLogging());
        mTwitterManager = new TwitterManager(getApplication(), new TwitterManagerTestConfig());
        mTwitterRepository = new TwitterRemoteRepository(mTwitterManager, "http://localhost:8378/");
    }

    public void testFindHomeTweets_noPage() throws Exception {
        // Setup.
        final int lPageCount = 5;
        final int lPageSize = 20;
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        when(getServer().getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_empty.json");
        // Run.
        subscribeAndWait(mTwitterRepository.findTweets(null, lTimeGap, lPageCount, lPageSize), mTweetPageObserver);
        // Verify server calls.
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                   not(hasQueryParam("max_id")))));
        // Verify empty page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        Mockito.verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        Mockito.verify(mTweetPageObserver).onCompleted();

        TweetPageResponse lPageResponse = lTweetPageResponseCaptor.getAllValues().get(0);
        TweetPageData.checkTweetPage_empty(lPageResponse, lTimeGap);

        Mockito.verifyNoMoreInteractions(getServer(), mTweetPageObserver);
    }

    public void testFindHomeTweets_singlePage_moreAvailable() throws Exception {
        // Setup.
        final int lPageCount = 1; // Single page
        final int lPageSize = 20; // More page available since returned page will be full.
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        when(getServer().getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_02-1.json");
        // Run.
        subscribeAndWait(mTwitterRepository.findTweets(null, lTimeGap, lPageCount, lPageSize), mTweetPageObserver);
        // Verify server calls.
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                   not(hasQueryParam("max_id")))));
        // Verify page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        Mockito.verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        Mockito.verify(mTweetPageObserver).onCompleted();

        TweetPageResponse lPageResponse = lTweetPageResponseCaptor.getAllValues().get(0);
        TweetPageData.checkTweetPage_02_1(lPageResponse, lTimeGap);

        Mockito.verifyNoMoreInteractions(getServer(), mTweetPageObserver);
    }

    public void testFindHomeTweets_severalPages_noMoreAvailable() throws Exception {
        // Setup.
        final int lPageCount = 5; // Several pages. Not all will be returned.
        final int lPageSize = 20; // No more page available since last returned page will not be full.
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        when(getServer().getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_02-1.json")
                                                                       .thenReturn("twitter/ctx_tweet_02-2.json")
                                                                       .thenReturn("twitter/ctx_tweet_02-3.json");
        // Run.
        subscribeAndWait(mTwitterRepository.findTweets(null, lTimeGap, lPageCount, lPageSize), mTweetPageObserver);
        // Verify server calls.
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                   not(hasQueryParam("max_id")))));
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                   hasQueryParam("max_id", TweetPageData.OLDEST_02_1 - 1))));
        Mockito.verify(getServer()).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                   hasQueryParam("max_id", TweetPageData.OLDEST_02_2 - 1))));
        // Verify pages received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        Mockito.verify(mTweetPageObserver, times(3)).onNext(lTweetPageResponseCaptor.capture());
        Mockito.verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lPageResponse = lTweetPageResponseCaptor.getAllValues();
        TweetPageData.checkTweetPage_02_1(lPageResponse.get(0), lTimeGap);
        TweetPageData.checkTweetPage_02_2(lPageResponse.get(1), lPageResponse.get(0).remainingGap());
        TweetPageData.checkTweetPage_02_3(lPageResponse.get(2), lPageResponse.get(1).remainingGap());

        Mockito.verifyNoMoreInteractions(getServer(), mTweetPageObserver);
    }
}
