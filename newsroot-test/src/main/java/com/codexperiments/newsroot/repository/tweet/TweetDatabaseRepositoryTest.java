package com.codexperiments.newsroot.repository.tweet;

import static com.codexperiments.newsroot.test.data.TweetPageData.PAGE_SIZE;
import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndReturn;
import static com.codexperiments.newsroot.test.helper.RxTest.subscribeAndWait;
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
import org.mockito.Mockito;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import com.codexperiments.newsroot.data.tweet.TweetDAO;
import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.test.TestCase;
import com.codexperiments.newsroot.test.TestModule;
import com.codexperiments.newsroot.test.data.TweetPageData;

import dagger.Module;
import dagger.Provides;

public class TweetDatabaseRepositoryTest extends TestCase {
    @Inject TweetDatabase mDatabase;
    @Inject TweetDatabaseRepository mTweetDatabaseRepository;
    @Inject TweetRemoteRepository mTweetRemoteRepository;
    @Inject Observer<TweetPageResponse> mTweetPageObserver;

    @Module(includes = TestModule.class, injects = TweetDatabaseRepositoryTest.class, overrides = true)
    static class LocalModule {
        @Provides
        public TweetDatabaseRepository provideTweetDatabaseRepository(TweetDatabase pTweetDatabase,
                                                                      TweetDAO pTweetDAO,
                                                                      TweetRemoteRepository pMockRepository)
        {
            return new TweetDatabaseRepository(pTweetDatabase, pTweetDAO, pMockRepository);
        }

        @Provides
        @Singleton
        public TweetRemoteRepository provideTweetRemoteRepository() {
            return mock(TweetRemoteRepository.class);
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
        Mockito.reset(mTweetRemoteRepository);
    }

    public void testFindTweetsInCache_noPage() throws Exception {
        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");

        // SCENARIO: No page paged stored in database.
        subscribeAndWait(mTweetDatabaseRepository.findTweetsInCache(lTimeline, PAGE_SIZE, Observable.from(lTimeGap)),
                         mTweetPageObserver);
        // Verify empty page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        TweetPageResponse lPageResponse = lTweetPageResponseCaptor.getAllValues().get(0);
        TweetPageData.checkTweetPage_empty(lPageResponse, lTimeGap);

        verifyNoMoreInteractions(mTweetPageObserver);
    }

    public void testFindTweetsInCache_severalPages() throws Exception {
        mDatabase.executeScriptFromAssets("sql/ctx_timeline_02.sql");

        final TimeGap lTimeGap = TimeGap.initialTimeGap();
        final Timeline lTimeline = mTweetDatabaseRepository.findTimeline("Test");
        PublishSubject<TimeGap> lTimeGaps = PublishSubject.create();

        // SCENARIO: A few pages are returned from database (some full and some partial).
        CountDownLatch latch = subscribeAndReturn(mTweetDatabaseRepository.findTweetsInCache(lTimeline, PAGE_SIZE, lTimeGaps),
                                                  mTweetPageObserver);
        lTimeGaps.onNext(TimeGap.initialTimeGap());
        lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_1 - 1));
        lTimeGaps.onNext(TimeGap.pastTimeGap(TweetPageData.OLDEST_02_2 - 1));
        lTimeGaps.onCompleted();
        latch.await();

        // Verify empty page received.
        ArgumentCaptor<TweetPageResponse> lTweetPageResponseCaptor = ArgumentCaptor.forClass(TweetPageResponse.class);
        verify(mTweetPageObserver, times(3)).onNext(lTweetPageResponseCaptor.capture());
        verify(mTweetPageObserver).onCompleted();

        List<TweetPageResponse> lTweetPageResponseArgs = lTweetPageResponseCaptor.getAllValues();
        TweetPageData.checkTweetPage_02_1(lTweetPageResponseCaptor.getAllValues().get(0), lTimeGap);
        TweetPageData.checkTweetPage_02_2(lTweetPageResponseArgs.get(1), lTweetPageResponseArgs.get(0).remainingGap());
        TweetPageData.checkTweetPage_02_3(lTweetPageResponseArgs.get(2), lTweetPageResponseArgs.get(1).remainingGap());

        verifyNoMoreInteractions(mTweetPageObserver);
    }
}
