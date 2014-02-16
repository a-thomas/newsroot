package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.manager.tweet.TweetQuery.URL_HOME;
import static com.codexperiments.newsroot.test.data.TweetPageData.OLDEST_02_1;
import static com.codexperiments.newsroot.test.data.TweetPageData.OLDEST_02_2;
import static com.codexperiments.newsroot.test.data.TweetPageData.PAGE_SIZE;
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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mockito.ArgumentCaptor;

import rx.Observer;

import com.codexperiments.newsroot.data.tweet.TweetDAO;
import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.test.TestCase;
import com.codexperiments.newsroot.test.TestModule;
import com.codexperiments.newsroot.test.data.TweetPageData;
import com.codexperiments.newsroot.test.server.MockServer;

import dagger.Module;
import dagger.Provides;

public class TweetRepositoryIntegrationTest extends TestCase {
    @Inject TweetDatabase mDatabase;
    @Inject TweetDatabaseRepository mTweetDatabaseRepository;
    @Inject TweetRemoteRepository mTweetRemoteRepository;
    @Inject Observer<TweetPageResponse> mTweetPageObserver;

    @Module(includes = TestModule.class, injects = TweetRepositoryIntegrationTest.class, overrides = true)
    static class LocalModule {
        @Provides
        @Singleton
        public TweetDatabaseRepository provideTweetDatabaseRepository(TweetRemoteRepository pTweetRemoteRepository,
                                                                      TweetDatabase pTweetDatabase,
                                                                      TweetDAO pTweetDAO)
        {
            return new TweetDatabaseRepository(pTweetDatabase, pTweetDAO, pTweetRemoteRepository);
        }

        @Provides
        public TweetRemoteRepository provideTweetRemoteRepository(TweetManager pTweetManager) {
            return new TweetRemoteRepository(pTweetManager, "http://localhost:" + MockServer.PORT + "/");
        }

        @Provides
        @Singleton
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

    public void testFindTweets_fromRemote() throws Exception {
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");

        // SCENARIO: Pages are returned from server.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_02-1.json")
                               .thenReturn("twitter/ctx_tweet_02-2.json")
                               .thenReturn("twitter/ctx_tweet_02-3.json");
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, 5, PAGE_SIZE), mTweetPageObserver);

        // Verify server calls.
        verify(server()).getResponse(hasUrl(URL_HOME, hasParam("count", PAGE_SIZE), not(hasParam("max_id"))));
        verify(server()).getResponse(hasUrl(URL_HOME, hasParam("count", PAGE_SIZE), hasParam("max_id", OLDEST_02_1 - 1)));
        verify(server()).getResponse(hasUrl(URL_HOME, hasParam("count", PAGE_SIZE), hasParam("max_id", OLDEST_02_2 - 1)));

        // Verify empty page received.
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
