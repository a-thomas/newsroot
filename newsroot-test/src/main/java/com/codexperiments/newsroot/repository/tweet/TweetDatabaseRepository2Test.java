package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.test.data.TweetPageData.PAGE_SIZE;
import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndReturn;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.hasQueryParam;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.hasUrl;
import static com.codexperiments.newsroot.test.server.MockServerMatchers.whenRequestOn;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.mockito.ArgumentCaptor;

import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.util.functions.Func1;

import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.manager.tweet.TweetQuery;
import com.codexperiments.newsroot.test.TestCase;
import com.codexperiments.newsroot.test.TestModule;
import com.codexperiments.newsroot.test.data.TweetPageData;
import com.codexperiments.newsroot.test.server.MockServer;
import com.codexperiments.rx.Rxt;

import dagger.Module;
import dagger.Provides;

public class TweetDatabaseRepository2Test extends TestCase {
    @Inject TweetDatabase mDatabase;
    @Inject TweetDatabaseRepository mTweetDatabaseRepository;
    @Inject TweetRemoteRepository mTweetRemoteRepository;
    @Inject Observer<TweetPageResponse> mTweetPageObserver;

    @Module(includes = TestModule.class, injects = TweetDatabaseRepository2Test.class, overrides = true)
    static class LocalModule {
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
        // Mockito.reset(mTweetRemoteRepository);
    }

    public void testFindHomeTweets_severalPages() throws Exception {
        // Setup.
        final int lPageCount = 5; // Several pages. Not all will be returned.
        final int lPageSize = 20; // No more page available since last returned page will not be full.
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        PublishSubject<TimeGap> lTimeGaps = PublishSubject.create();

        // SCENARIO: Several pages returned from the server, last being not full.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_02-1.json")
                               .thenReturn("twitter/ctx_tweet_02-2.json")
                               .thenReturn("twitter/ctx_tweet_02-3.json");
        CountDownLatch latch = subscribeAndReturn(mTweetRemoteRepository.findTweetsIM(null, lPageSize, lTimeGaps),
                                                  mTweetPageObserver);
        lTimeGaps.onNext(TimeGap.initialTimeGap());
        lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_1 - 1));
        lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_2 - 1));
        lTimeGaps.onCompleted();
        latch.await();

        // Verify server calls.
        verify(server()).getResponse(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                   not(hasQueryParam("max_id")))));
        verify(server()).getResponse(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                   hasQueryParam("max_id", TweetPageData.OLDEST_02_1 - 1))));
        verify(server()).getResponse(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                   hasQueryParam("max_id", TweetPageData.OLDEST_02_2 - 1))));
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

    /**
     * No tweet is returned, only an empty page.
     */
    public void testFindHomeTweets_111111() throws Exception {
        // mDatabase.executeAssetScript("sql/ctx_timeline_02.sql");

        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");
        final BehaviorSubject<TimeGap> lTimeGaps = BehaviorSubject.create(lTimeGap);

        // SCENARIO: An empty page is returned by server.
        whenRequestOn(server()).thenReturn("twitter/ctx_tweet_02-1.json")
                               .thenReturn("twitter/ctx_tweet_02-2.json")
                               .thenReturn("twitter/ctx_tweet_02-3.json");

        Func1<TweetPageResponse, Boolean> lPageIsFull = new Func1<TweetPageResponse, Boolean>() {
            public Boolean call(TweetPageResponse pTweetPageResponse) {
                return pTweetPageResponse != null && pTweetPageResponse.tweetPage().isFull();
            }
        };
        Func1<TweetPageResponse, Boolean> lEmptyPages = new Func1<TweetPageResponse, Boolean>() {
            public Boolean call(TweetPageResponse pTweetPageResponse) {
                return pTweetPageResponse != null && !pTweetPageResponse.tweetPage().isEmpty();
            }
        };

        Observable<TweetPageResponse> lFromDatabase = mTweetDatabaseRepository.findTweetsInCache(lTimeline, PAGE_SIZE, lTimeGaps);
        lFromDatabase = Rxt.takeWhileInclusive(lFromDatabase, lPageIsFull).filter(lEmptyPages);
        Observable<TweetPageResponse> lFromRepo = mTweetRemoteRepository.findTweetsIM(null, PAGE_SIZE, lTimeGaps);
        lFromRepo = Rxt.takeWhileInclusive(lFromRepo, lPageIsFull).filter(lEmptyPages);
        Observable<TweetPageResponse> lRequest = Observable.concat(lFromDatabase, lFromRepo);

        CountDownLatch latch = subscribeAndReturn(lRequest, new Observer<TweetPageResponse>() {
            public void onNext(TweetPageResponse pTweetPageResponse) {
                lTimeGaps.onNext(lTimeGap.remainingGap(pTweetPageResponse.tweetPage().timeRange()));
                mTweetPageObserver.onNext(pTweetPageResponse);
            }

            public void onCompleted() {
                mTweetPageObserver.onCompleted();
            }

            public void onError(Throwable pThrowable) {
                mTweetPageObserver.onError(pThrowable);
            }
        });
        // lTimeGaps.onNext(TimeGap.initialTimeGap());
        // lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_1 - 1));
        // lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_2 - 1));
        // lTimeGaps.onCompleted();
        latch.await();

        // Verify server calls.
        verify(server()).getResponse(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                   not(hasQueryParam("max_id")))));
        verify(server()).getResponse(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                   hasQueryParam("max_id", TweetPageData.OLDEST_02_1 - 1))));
        verify(server()).getResponse(argThat(allOf(hasUrl(TweetQuery.URL_HOME),
                                                   hasQueryParam("count", TweetPageData.PAGE_SIZE),
                                                   hasQueryParam("max_id", TweetPageData.OLDEST_02_2 - 1))));

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
