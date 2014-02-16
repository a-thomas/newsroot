package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.manager.tweet.TweetQuery.URL_HOME;
import static com.codexperiments.newsroot.test.data.TweetPageData.OLDEST_02_1;
import static com.codexperiments.newsroot.test.data.TweetPageData.OLDEST_02_2;
import static com.codexperiments.newsroot.test.data.TweetPageData.PAGE_SIZE;
import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndReturn;
import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndWait;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.hasParam;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.hasUrl;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.whenRequestOn;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.mockito.ArgumentCaptor;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.test.TestCase;
import com.codexperiments.newsroot.test.TestModule;
import com.codexperiments.newsroot.test.data.TweetPageData;
import com.codexperiments.newsroot.test.server.MockServer;

import dagger.Module;
import dagger.Provides;

public class TweetRemoteRepositoryTest extends TestCase {
    @Inject TweetRemoteRepository mTweetRemoteRepository;
    @Inject Observer<TweetPageResponse> mTweetPageObserver;

    @Module(includes = TestModule.class, injects = TweetRemoteRepositoryTest.class, overrides = true)
    static class LocalModule {
        @Provides
        public TweetRemoteRepository provideTweetRemoteRepository(TweetManager pTweetManager) {
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

    public void testFindTweets_noPage() throws Exception {
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        // SCENARIO: An empty page is returned by server.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_empty.json");
        subscribeAndWait(mTweetRemoteRepository.findTweets(null, PAGE_SIZE, Observable.from(lTimeGap)), mTweetPageObserver);

        // Verify server calls.
        verify(server()).getResponse(hasUrl(URL_HOME, hasParam("count", PAGE_SIZE), not(hasParam("max_id"))));
        // Verify empty page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        TweetPageResponse lPageResponse = lTweetPageResponseCaptor.getAllValues().get(0);
        TweetPageData.checkTweetPage_empty(lPageResponse, lTimeGap);

        verifyNoMoreInteractions(mTweetPageObserver);
    }

    public void testFindTweets_severalPages() throws Exception {
        // Setup.
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        PublishSubject<TimeGap> lTimeGaps = PublishSubject.create();

        // SCENARIO: Several pages returned from the server, last being not full.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_02-1.json")
                               .thenReturn("twitter/ctx_tweet_02-2.json")
                               .thenReturn("twitter/ctx_tweet_02-3.json");
        CountDownLatch latch = subscribeAndReturn(mTweetRemoteRepository.findTweets(null, PAGE_SIZE, lTimeGaps),
                                                  mTweetPageObserver);
        lTimeGaps.onNext(TimeGap.initialTimeGap());
        lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_1 - 1));
        lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_2 - 1));
        lTimeGaps.onCompleted();
        latch.await();

        // Verify server calls.
        verify(server()).getResponse(hasUrl(URL_HOME, hasParam("count", PAGE_SIZE), not(hasParam("max_id"))));
        verify(server()).getResponse(hasUrl(URL_HOME, hasParam("count", PAGE_SIZE), hasParam("max_id", OLDEST_02_1 - 1)));
        verify(server()).getResponse(hasUrl(URL_HOME, hasParam("count", PAGE_SIZE), hasParam("max_id", OLDEST_02_2 - 1)));
        // Verify pages received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver, times(3)).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lTweetPageResponseArgs = lTweetPageResponseCaptor.getAllValues();
        TweetPageData.checkTweetPage_02_1(lTweetPageResponseCaptor.getAllValues().get(0), lTimeGap);
        TweetPageData.checkTweetPage_02_2(lTweetPageResponseArgs.get(1), lTweetPageResponseArgs.get(0).remainingGap());
        TweetPageData.checkTweetPage_02_3(lTweetPageResponseArgs.get(2), lTweetPageResponseArgs.get(1).remainingGap());

        verifyNoMoreInteractions(server(), mTweetPageObserver);
    }
}
