package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndNotWait;
import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndWait;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.hasQueryParam;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.hasUrl;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.whenRequestOn;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import javax.inject.Inject;

import org.mockito.ArgumentCaptor;

import rx.Observable;
import rx.Observer;
import android.util.Log;

import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.test.TestCase;
import com.codexperiments.newsroot.test.TestModule;
import com.codexperiments.newsroot.test.data.TweetPageData;
import com.codexperiments.newsroot.test.server.MockServer;

import dagger.Module;
import dagger.Provides;

public class TweetRemoteRepositoryTest extends TestCase {
    @Inject TweetRepository mTweetRemoteRepository;
    @Inject Observer<TweetPageResponse> mTweetPageObserver;

    @Module(includes = TestModule.class, injects = TweetRemoteRepositoryTest.class, overrides = true)
    static class LocalModule {
        @Provides
        public TweetRepository provideTweetRemoteRepository(TweetManager pTweetManager) {
            return new TweetRemoteRepository(pTweetManager, "http://localhost:" + MockServer.PORT + "/");
        }

        @Provides
        @SuppressWarnings("unchecked")
        public Observer<TweetPageResponse> provideTweetPageObserver() {
            return mock(Observer.class, withSettings().verboseLogging());
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        inject(new LocalModule());
    }

    public void testFindHomeTweets_noPage() throws Exception {
        final int lPageCount = 5;
        final int lPageSize = 20;
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        // SCENARIO: An empty page is returned by server.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_empty.json");
        subscribeAndWait(mTweetRemoteRepository.findTweets(null, lTimeGap, lPageCount, lPageSize), mTweetPageObserver);

        // Verify server calls.
        verify(server()).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                        hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                        not(hasQueryParam("max_id")))));
        // Verify empty page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lTweetPageResponseArgs = lTweetPageResponseCaptor.getAllValues();
        assertThat(lTweetPageResponseArgs.size(), equalTo(1));
        TweetPageData.checkTweetPage_empty(lTweetPageResponseArgs.get(0), lTimeGap);

        verifyNoMoreInteractions(server(), mTweetPageObserver);
    }

    public void testFindHomeTweets_singlePage_moreAvailable() throws Exception {
        // Setup.
        final int lPageCount = 1; // Single page
        final int lPageSize = 20; // More page available since returned page will be full.
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        // SCENARIO: A full page is returned from the server.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_02-1.json");
        subscribeAndWait(mTweetRemoteRepository.findTweets(null, lTimeGap, lPageCount, lPageSize), mTweetPageObserver);

        // Verify server calls.
        verify(server()).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                        hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                        not(hasQueryParam("max_id")))));
        // Verify page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        TweetPageResponse lPageResponse = lTweetPageResponseCaptor.getAllValues().get(0);
        TweetPageData.checkTweetPage_02_1(lPageResponse, lTimeGap);

        verifyNoMoreInteractions(server(), mTweetPageObserver);
    }

    public void testFindHomeTweets_severalPages_noMoreAvailable() throws Exception {
        // Setup.
        final int lPageCount = 5; // Several pages. Not all will be returned.
        final int lPageSize = 20; // No more page available since last returned page will not be full.
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        // SCENARIO: Several pages returned from the server, last being not full.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_02-1.json")
                               .thenReturn("twitter/ctx_tweet_02-2.json")
                               .thenReturn("twitter/ctx_tweet_02-3.json");
        subscribeAndWait(mTweetRemoteRepository.findTweets(null, lTimeGap, lPageCount, lPageSize), mTweetPageObserver);

        // Verify server calls.
        verify(server()).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                        hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                        not(hasQueryParam("max_id")))));
        verify(server()).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                        hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                        hasQueryParam("max_id", TweetPageData.OLDEST_02_1 - 1))));
        verify(server()).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                        hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                        hasQueryParam("max_id", TweetPageData.OLDEST_02_2 - 1))));
        // Verify pages received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver, times(3)).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lTweetPageResponseArgs = lTweetPageResponseCaptor.getAllValues();
        assertThat(lTweetPageResponseArgs.size(), equalTo(3));
        TweetPageData.checkTweetPage_02_1(lTweetPageResponseCaptor.getAllValues().get(0), lTimeGap);
        TweetPageData.checkTweetPage_02_2(lTweetPageResponseArgs.get(1), lTweetPageResponseArgs.get(0).remainingGap());
        TweetPageData.checkTweetPage_02_3(lTweetPageResponseArgs.get(2), lTweetPageResponseArgs.get(1).remainingGap());

        verifyNoMoreInteractions(server(), mTweetPageObserver);
    }

    public void testFindHomeTweets_severalPages_noMoreAvailable_multi() throws Exception {
        // Setup.
        final int lPageCount = 5; // Several pages. Not all will be returned.
        final int lPageSize = 20; // No more page available since last returned page will not be full.
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        // SCENARIO: Several pages returned from the server, last being not full.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_02-1.json")
                               .thenReturn("twitter/ctx_tweet_02-1.json")
                               .thenReturn("twitter/ctx_tweet_02-2.json")
                               .thenReturn("twitter/ctx_tweet_02-2.json")
                               .thenReturn("twitter/ctx_tweet_02-3.json")
                               .thenReturn("twitter/ctx_tweet_02-3.json");
        Observable<TweetPageResponse> resp = mTweetRemoteRepository.findTweets(null, lTimeGap, lPageCount, lPageSize);
        Observable<TweetPageResponse> resp2 = mTweetRemoteRepository.findTweets(null, lTimeGap, lPageCount, lPageSize);
        subscribeAndNotWait(resp, new Observer<TweetPageResponse>() {
            @Override
            public void onCompleted() {
                Log.w("XXX", "completed");
            }

            @Override
            public void onError(Throwable pE) {
                Log.w("XXX", "error ", pE);
            }

            @Override
            public void onNext(TweetPageResponse pArgs) {
                Log.w("XXX", "next " + pArgs);
            }
        });
        subscribeAndWait(resp, mTweetPageObserver);
        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }

        // Verify server calls.
        verify(server(), times(2)).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                                  hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                  not(hasQueryParam("max_id")))));
        verify(server(), times(2)).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                                  hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                  hasQueryParam("max_id", TweetPageData.OLDEST_02_1 - 1))));
        verify(server(), times(2)).getResponseAsset(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                                  hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                                  hasQueryParam("max_id", TweetPageData.OLDEST_02_2 - 1))));
        // Verify pages received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver, times(3)).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lTweetPageResponseArgs = lTweetPageResponseCaptor.getAllValues();
        assertThat(lTweetPageResponseArgs.size(), equalTo(3));
        TweetPageData.checkTweetPage_02_1(lTweetPageResponseCaptor.getAllValues().get(0), lTimeGap);
        TweetPageData.checkTweetPage_02_2(lTweetPageResponseArgs.get(1), lTweetPageResponseArgs.get(0).remainingGap());
        TweetPageData.checkTweetPage_02_3(lTweetPageResponseArgs.get(2), lTweetPageResponseArgs.get(1).remainingGap());
        assertThat(lTweetPageResponseCaptor.getAllValues().size(), equalTo(3));

        verifyNoMoreInteractions(server(), mTweetPageObserver);
    }
}
