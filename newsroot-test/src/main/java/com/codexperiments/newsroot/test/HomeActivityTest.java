package com.codexperiments.newsroot.test;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.mockito.Mockito;
import org.simpleframework.http.Query;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;

import com.codexperiments.newsroot.common.event.AndroidEventBus;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.repository.twitter.TwitterAPI;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.newsroot.test.server.MockBackend;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.newsroot.ui.activity.HomeActivity;
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.robolabor.task.android.AndroidTaskManager;
import com.codexperiments.robolabor.task.android.AndroidTaskManagerConfig;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {
    public static final int DEFAULT_TIMEOUT_MS = 1000000;

    private Application mApplication;
    private TwitterDatabase mDatabase;
    private MockBackend.Config mServerConfig;
    private MockBackend.Server mServer;

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;
    private TwitterAPI mTwitterAPI;
    private TwitterRepository mTwitterRepository;

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        mDatabase = new TwitterDatabase(mApplication);
        mDatabase.recreate();

        mServerConfig = mock(MockBackend.Config.class);
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);

        mEventBus = new AndroidEventBus();
        mTaskManager = new AndroidTaskManager(mApplication, new AndroidTaskManagerConfig(mApplication));
        // mTwitterManager = mock(TwitterManager.class);
        mTwitterManager = new TwitterManager(mApplication, mEventBus, new TwitterManager.Config() {
            public String getHost() {
                return "http://localhost:8378/";
            }

            public String getConsumerKey() {
                return "3Ng9QGTB7EpZCHDOIT2jg";
            }

            public String getConsumerSecret() {
                return "OolXzfWdSF6uMdgt2mvLNpDl4HOA1JNlN487LvDUA4";
            }

            public String getCallbackURL() {
                return "oauth://newsroot-callback";
            }
        });
        // mTwitterAPI = mock(TwitterAPI.class);
        mTwitterAPI = new TwitterAPI(mTwitterManager, "http://localhost:8378/");
        mTwitterRepository = new TwitterRepository(mApplication, mEventBus, mTaskManager, mTwitterManager, mTwitterAPI, mDatabase);

        // Record
        // when(mTwitterManager.isAuthorized()).thenReturn(true);
        // when(mTwitterManager.queryHome()).thenReturn(new TwitterQuery(lRepositoryConfig.getHost(), pQuery));
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mServer.stop();
        super.tearDown();
    }

    @SuppressWarnings("unchecked")
    public void testFindNewTweets_nonEmptyTimeline_withResults()
        throws IOException, TwitterAccessException, InterruptedException, Exception
    {
        // Setup
        final CountDownLatch lFinished = new CountDownLatch(3);
        final Observer<TweetPage> lTweetPageObserver = Mockito.mock(Observer.class, withSettings().verboseLogging());
        mDatabase.executeAssetScript("twitter/ctx_timeline_01.sql", getInstrumentation().getContext());
        mDatabase.executeAssetScript("twitter/ctx_timeline.sql", getInstrumentation().getContext());
        final Timeline lTimeline = new Timeline(1, -1l, -1l);

        // Record
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/ctx_tweet_02-1.json")
                                                                                    .thenReturn("twitter/ctx_tweet_02-2.json")
                                                                                    .thenReturn("twitter/ctx_tweet_02-3.json");
        // Run
        // Observable<Observable<News>> lNews = mTwitterRepository.downloadLatestNews(lTimeline);
        Observable<TweetPage> lNews = mTwitterRepository.findOlderNews(lTimeline);
        assertThat(lNews, notNullValue());
        mockSubscribe(lNews, lTweetPageObserver, lFinished);

        lFinished.await();
        Mockito.verify(lTweetPageObserver, times(3)).onNext(argThat(any(TweetPage.class)));
        Mockito.verify(lTweetPageObserver).onCompleted();
        Mockito.verify(lTweetPageObserver, never()).onError(argThat(any(Throwable.class)));

        Mockito.verifyNoMoreInteractions(lTweetPageObserver, lTweetPageObserver);
        tearDown();
        setUp();
    }

    public <T> Observer<T> mockObserver(final Observer<T> pObserver) {
        return new Observer<T>() {
            public void onNext(T pValue) {
                pObserver.onNext(pValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
            }

            public void onError(Throwable pThrowable) {
                pObserver.onError(pThrowable);
            }
        };
    }

    public <T> Subscription mockSubscribe(final Observable<T> pObservable,
                                          final Observer<T> pObserver,
                                          final CountDownLatch pTestEnded)
    {
        return pObservable.observeOn(AndroidScheduler.getInstance()).subscribe(new Observer<T>() {
            public void onNext(T pPageValue) {
                pObserver.onNext(pPageValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
                pTestEnded.countDown();
            }

            public void onError(Throwable pThrowable) {
                pObserver.onError(pThrowable);
            }
        });
    }
}
