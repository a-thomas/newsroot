package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.test.data.TweetPageData.PAGE_SIZE;
import static com.codexperiments.newsroot.test.helper.RxTest.scheduleOnComplete;
import static com.codexperiments.newsroot.test.helper.RxTest.scheduleOnNext;
import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndWait;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import android.database.sqlite.SQLiteConstraintException;

import com.codexperiments.newsroot.data.tweet.TweetDAO;
import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.manager.tweet.TweetAccessException;
import com.codexperiments.newsroot.test.TestCase;
import com.codexperiments.newsroot.test.TestModule;
import com.codexperiments.newsroot.test.data.TweetPageData;
import com.codexperiments.newsroot.test.data.TweetPageResponseData;

import dagger.Module;
import dagger.Provides;

public class TweetDatabaseRepositoryTest extends TestCase {
    @Inject TweetRepository mTweetDatabaseRepository;
    @Inject @Named("mock") TweetRepository mTweetInnerRepository;
    @Inject Observer<TweetPageResponse> mTweetPageObserver;

    @Module(includes = TestModule.class, injects = TweetDatabaseRepositoryTest.class, overrides = true)
    static class LocalModule {
        @Provides
        public TweetRepository provideTweetDatabaseRepository(TweetDatabase pTweetDatabase,
                                                              TweetDAO pTweetDAO,
                                                              @Named("mock") TweetRepository pMockRepository)
        {
            return new TweetDatabaseRepository(pTweetDatabase, pTweetDAO, pMockRepository);
        }

        @Provides
        @Singleton
        @Named("mock")
        public TweetRepository provideMockRepository() {
            return mock(TweetRepository.class);
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
        Mockito.reset(mTweetInnerRepository);
    }

    /**
     * No tweet is returned, only an empty page.
     */
    public void testFindHomeTweets_noPage() throws Exception {
        final int lPageCount = 5;
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");
        final Observable<TweetPageResponse> lInnerResult = Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
            public Subscription onSubscribe(Observer<? super TweetPageResponse> pTweetPageResponseObserver) {
                scheduleOnNext(scheduler(), pTweetPageResponseObserver, TweetPageResponse.emptyResponse(lTimeGap, PAGE_SIZE), 100);
                scheduleOnComplete(scheduler(), pTweetPageResponseObserver, 200);
                scheduler().advanceTimeTo(500, TimeUnit.MILLISECONDS);
                return Subscriptions.empty();
            }
        });

        // SCENARIO: An empty page is returned by server.
        Mockito.when(mTweetInnerRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE)) //
               .thenReturn(lInnerResult);
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE), mTweetPageObserver);
        // Verify inner repository calls.
        verify(mTweetInnerRepository).findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE);
        // Verify empty page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        TweetPageResponse lPageResponse = lTweetPageResponseCaptor.getAllValues().get(0);
        TweetPageData.checkTweetPage_empty(lPageResponse, lTimeGap);

        verifyNoMoreInteractions(mTweetPageObserver);
    }

    /**
     * More page available since returned page will be full.
     */
    public void testFindHomeTweets_singlePage_moreAvailable() throws Exception {
        final int lPageCount = 1; // Single page
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final TweetPageResponse lServerResponse = TweetPageResponseData.fromAsset(getInstrumentation().getContext(),
                                                                                  lTimeGap,
                                                                                  "twitter/ctx_tweet_02-1.json");

        // SCENARIO: A full page is returned from the server.
        Mockito.when(mTweetInnerRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE))
               .thenReturn(TweetPageResponseData.asObservable(scheduler(), lServerResponse));
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE), mTweetPageObserver);
        // Verify inner repository calls.
        verify(mTweetInnerRepository).findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE);
        // Verify page received is the one returned from the server.
        verify(mTweetPageObserver).onNext(lServerResponse);
        verify(mTweetPageObserver).onCompleted();
        verifyNoMoreInteractions(mTweetInnerRepository, mTweetPageObserver);
        reset(mTweetInnerRepository, mTweetPageObserver);

        // SCENARIO: Server response has been cached and so data is now returned from the database.
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE), mTweetPageObserver);
        // Verify page returned by the database is the same as server response.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lTweetPageResponseArgs = lTweetPageResponseCaptor.getAllValues();
        assertThat(lTweetPageResponseArgs.size(), equalTo(1));
        TweetPageData.checkTweetPage_02_1(lTweetPageResponseArgs.get(0), lTimeGap); // Superficial data check.
        TweetPageData.checkTweetPageResponse(lTweetPageResponseArgs.get(0), lServerResponse); // Comparison to reference data.
        verifyNoMoreInteractions(mTweetInnerRepository, mTweetPageObserver);
    }

    /**
     * No more page available since last returned page will not be full.
     */
    public void testFindHomeTweets_severalPages_noMoreAvailable() throws Exception {
        final int lPageCount = 5; // Several pages. Not all will be returned.
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final TweetPageResponse lServerResponse1 = TweetPageResponseData.fromAsset(getInstrumentation().getContext(),
                                                                                   lTimeGap,
                                                                                   "twitter/ctx_tweet_02-1.json");
        final TweetPageResponse lServerResponse2 = TweetPageResponseData.fromAsset(getInstrumentation().getContext(),
                                                                                   lServerResponse1.remainingGap(),
                                                                                   "twitter/ctx_tweet_02-2.json");
        final TweetPageResponse lServerResponse3 = TweetPageResponseData.fromAsset(getInstrumentation().getContext(),
                                                                                   lServerResponse2.remainingGap(),
                                                                                   "twitter/ctx_tweet_02-3.json");

        // SCENARIO: Several pages returned from the server, last being not full.
        Mockito.when(mTweetInnerRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE))
               .thenReturn(TweetPageResponseData.asObservable(scheduler(), lServerResponse1, lServerResponse2, lServerResponse3));
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE), mTweetPageObserver);

        // Verify inner repository calls.
        verify(mTweetInnerRepository).findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE);
        // Verify page received is the one returned from the server.
        verify(mTweetPageObserver).onNext(lServerResponse1);
        verify(mTweetPageObserver).onNext(lServerResponse2);
        verify(mTweetPageObserver).onNext(lServerResponse3);
        verify(mTweetPageObserver).onCompleted();
        verifyNoMoreInteractions(mTweetInnerRepository, mTweetPageObserver);
        reset(mTweetInnerRepository, mTweetPageObserver);

        // SCENARIO: Server response has been cached and so data is now returned from the database.
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE), mTweetPageObserver);
        // Verify pages returned by the database are the same as server response.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver, times(3)).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lTweetPageResponseArgs = lTweetPageResponseCaptor.getAllValues();
        assertThat(lTweetPageResponseArgs.size(), equalTo(3));
        TweetPageData.checkTweetPage_02_1(lTweetPageResponseArgs.get(0), lTimeGap); // Superficial data check.
        TweetPageData.checkTweetPageResponse(lTweetPageResponseArgs.get(0), lServerResponse1); // Comparison to reference data.
        TweetPageData.checkTweetPage_02_2(lTweetPageResponseArgs.get(1), lTweetPageResponseArgs.get(0).remainingGap());
        TweetPageData.checkTweetPageResponse(lTweetPageResponseArgs.get(1), lServerResponse2);
        TweetPageData.checkTweetPage_02_3(lTweetPageResponseArgs.get(2), lTweetPageResponseArgs.get(1).remainingGap());
        TweetPageData.checkTweetPageResponse(lTweetPageResponseArgs.get(2), lServerResponse3);
        verifyNoMoreInteractions(mTweetInnerRepository, mTweetPageObserver);
    }

    public void testFindHomeTweets_constraintException() throws Exception {
        final int lPageCount = 5;
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final TweetPageResponse lServerResponse1 = TweetPageResponseData.fromAsset(getInstrumentation().getContext(),
                                                                                   lTimeGap,
                                                                                   "twitter/ctx_tweet_02-1.json");
        final TweetPageResponse lServerResponse2 = TweetPageResponseData.fromAsset(getInstrumentation().getContext(),
                                                                                   lTimeGap,
                                                                                   "twitter/ctx_tweet_02-1.json");

        // SCENARIO: A full page is returned from the server.
        Mockito.when(mTweetInnerRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE))
               .thenReturn(TweetPageResponseData.asObservable(scheduler(), lServerResponse1, lServerResponse2));
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE), mTweetPageObserver);

        // Verify inner repository calls.
        verify(mTweetInnerRepository).findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE);
        // Verify page received is the one returned from the server.
        verify(mTweetPageObserver).onNext(lServerResponse1);
        verify(mTweetPageObserver).onError(argThat(isA(SQLiteConstraintException.class)));
        verifyNoMoreInteractions(mTweetInnerRepository, mTweetPageObserver);
    }

    public void testFindHomeTweets_serverException() throws Exception {
        final int lPageCount = 5;
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");
        final TimeGap lTimeGap = TimeGap.initialTimeGap();

        // SCENARIO: A full page is returned from the server.
        Mockito.when(mTweetInnerRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE))
               .thenReturn(TweetPageResponseData.asThrowable(scheduler(), TweetAccessException.from(new IOException())));
        subscribeAndWait(mTweetDatabaseRepository.findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE), mTweetPageObserver);

        // Verify inner repository calls.
        verify(mTweetInnerRepository).findTweets(lTimeline, lTimeGap, lPageCount, PAGE_SIZE);
        // Verify page received is the one returned from the server.
        verify(mTweetPageObserver).onError(argThat(isA(TweetAccessException.class)));
        verifyNoMoreInteractions(mTweetInnerRepository, mTweetPageObserver);
    }
}
