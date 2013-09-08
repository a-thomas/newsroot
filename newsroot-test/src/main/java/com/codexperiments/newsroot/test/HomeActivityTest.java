package com.codexperiments.newsroot.test;

import static com.codexperiments.newsroot.test.server.MockBackendMatchers.hasQueryParam;
import static com.codexperiments.newsroot.test.server.MockBackendMatchers.hasUrl;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.simpleframework.http.Request;

import rx.Observable;
import rx.Observer;
import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;

import com.codexperiments.newsroot.common.event.AndroidEventBus;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.repository.twitter.TwitterAPI;
import com.codexperiments.newsroot.repository.twitter.TwitterQuery;
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
    private MockBackend.Handler mServerConfig;
    private MockBackend.Server mServer;

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;
    private TwitterAPI mTwitterAPI;
    private TwitterRepository mTwitterRepository;

    private Observer<TweetPage> mTweetPageObserver;

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        mApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        mDatabase = new TwitterDatabase(mApplication);
        mDatabase.recreate();

        mServerConfig = mock(MockBackend.Handler.class);
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);

        mEventBus = new AndroidEventBus();
        mTaskManager = new AndroidTaskManager(mApplication, new AndroidTaskManagerConfig(mApplication));
        mTwitterManager = new TwitterManager(mApplication, new TwitterManager.Config() {
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
        mTweetPageObserver = mock(Observer.class, withSettings().verboseLogging());
        // when(mTwitterManager.isAuthorized()).thenReturn(true);
        // when(mTwitterManager.queryHome()).thenReturn(new TwitterQuery(lRepositoryConfig.getHost(), pQuery));
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mServer.stop();
        super.tearDown();
    }

    public void testFindHomeTweets_getLessPagesThanAvailable() throws Exception {
        // Setup
        final int lPageCount = 1;
        final TimeGap lTimeGap = new TimeGap(-1L, -1L);

        when(mServerConfig.getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_02-1.json");

        // Run
        subscribe(mTwitterAPI.findHomeTweets(lTimeGap, lPageCount), mTweetPageObserver).await();

        // Verify
        ArgumentCaptor<TweetPage> lTweetPageArgs = ArgumentCaptor.forClass(TweetPage.class);

        Mockito.verify(mServerConfig).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                     hasQueryParam("count", 20),
                                                                     not(hasQueryParam("max_id")))));
        InOrder lObserverOrder = Mockito.inOrder(mTweetPageObserver);
        lObserverOrder.verify(mTweetPageObserver).onNext(lTweetPageArgs.capture());
        lObserverOrder.verify(mTweetPageObserver).onCompleted();
        Mockito.verifyNoMoreInteractions(mServerConfig, mTweetPageObserver);

        checkTweetPage_02_1(lTweetPageArgs.getValue(), lTimeGap);
    }

    public void testFindHomeTweets_getMorePagesThanAvailable() throws Exception {
        // Setup
        final int lPageCount = 5;
        final TimeGap lTimeGap = new TimeGap(-1L, -1L);

        when(mServerConfig.getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_02-1.json")
                                                                         .thenReturn("twitter/ctx_tweet_02-2.json")
                                                                         .thenReturn("twitter/ctx_tweet_02-3.json");

        // Run
        subscribe(mTwitterAPI.findHomeTweets(lTimeGap, lPageCount), mTweetPageObserver).await();

        // Verify
        ArgumentCaptor<TweetPage> lTweetPageArgs = ArgumentCaptor.forClass(TweetPage.class);

        InOrder lServerOrder = Mockito.inOrder(mServerConfig);
        lServerOrder.verify(mServerConfig).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                          hasQueryParam("count", 20),
                                                                          not(hasQueryParam("max_id")))));
        lServerOrder.verify(mServerConfig).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                          hasQueryParam("count", 20),
                                                                          hasQueryParam("max_id", 349473806655565800L - 1))));
        lServerOrder.verify(mServerConfig).getResponseAsset(argThat(allOf(hasUrl(TwitterQuery.URL_HOME),
                                                                          hasQueryParam("count", 20),
                                                                          hasQueryParam("max_id", 349457499134504960L - 1))));

        InOrder lObserverOrder = Mockito.inOrder(mTweetPageObserver);
        lObserverOrder.verify(mTweetPageObserver, times(3)).onNext(lTweetPageArgs.capture());
        lObserverOrder.verify(mTweetPageObserver).onCompleted();
        Mockito.verifyNoMoreInteractions(mServerConfig, mTweetPageObserver);

        List<TweetPage> lTweetPages = lTweetPageArgs.getAllValues();
        checkTweetPage_02_1(lTweetPages.get(0), lTimeGap);
        checkTweetPage_02_2(lTweetPages.get(1), lTweetPages.get(0).remainingGap());
        checkTweetPage_02_3(lTweetPages.get(2), lTweetPages.get(1).remainingGap());
    }

    public void testFindNewTweets_nonEmptyTimeline_withResults()
        throws IOException, TwitterAccessException, InterruptedException, Exception
    {
        // Setup
        mDatabase.executeAssetScript("twitter/ctx_timeline_01.sql", getInstrumentation().getContext());
        mDatabase.executeAssetScript("twitter/ctx_timeline.sql", getInstrumentation().getContext());
        final Timeline lTimeline = new Timeline(1, -1l, -1l);

        // Record
        when(mServerConfig.getResponseAsset(argThat(any(Request.class)))).thenReturn("twitter/ctx_tweet_02-1.json")
                                                                         .thenReturn("twitter/ctx_tweet_02-2.json")
                                                                         .thenReturn("twitter/ctx_tweet_02-3.json");
        // Run
        Observable<TweetPage> lNews = mTwitterRepository.findOlderNewsFromServer(lTimeline);
        assertThat(lNews, notNullValue());
        subscribe(lNews, mTweetPageObserver).await();

        Mockito.verify(mTweetPageObserver, times(3)).onNext(argThat(any(TweetPage.class)));
        Mockito.verify(mTweetPageObserver).onCompleted();

        Mockito.verifyNoMoreInteractions(mTweetPageObserver, mTweetPageObserver);
    }

    public <T> CountDownLatch subscribe(final Observable<T> pObservable, final Observer<T> pObserver) {
        final CountDownLatch lCompleted = new CountDownLatch(1);
        pObservable.observeOn(AndroidScheduler.getInstance()).subscribe(new Observer<T>() {
            public void onNext(T pPageValue) {
                pObserver.onNext(pPageValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
                lCompleted.countDown();
            }

            public void onError(Throwable pThrowable) {
                pObserver.onError(pThrowable);
            }
        });
        return lCompleted;
    }

    private void checkTweetPage_02_1(TweetPage pTweetPage, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPage.tweets();
        assertThat(lTweets, hasSize(20));
        assertThat(pTweetPage.timeGap(), equalTo(pTimeGap));
        assertThat(pTweetPage.remainingGap(), equalTo(pTimeGap.substract(lTweets, 20)));

        Tweet lFirstTweet = pTweetPage.tweets().get(0);
        assertThat(lFirstTweet.getId(), equalTo(349497246842241000L));

        Tweet lLastTweet = pTweetPage.tweets().get(19);
        assertThat(lLastTweet.getId(), equalTo(349473806655565800L));
    }

    private void checkTweetPage_02_2(TweetPage pTweetPage, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPage.tweets();
        assertThat(lTweets, hasSize(20));
        assertThat(pTweetPage.timeGap(), equalTo(pTimeGap));
        assertThat(pTweetPage.remainingGap(), equalTo(pTimeGap.substract(lTweets, 20)));

        Tweet lFirstTweet = pTweetPage.tweets().get(0);
        assertThat(lFirstTweet.getId(), equalTo(349471049735344100L));

        Tweet lLastTweet = pTweetPage.tweets().get(19);
        assertThat(lLastTweet.getId(), equalTo(349457499134504960L));
    }

    private void checkTweetPage_02_3(TweetPage pTweetPage, TimeGap pTimeGap) {
        List<Tweet> lTweets = pTweetPage.tweets();
        assertThat(pTweetPage.tweets(), hasSize(19));
        assertThat(pTweetPage.timeGap(), equalTo(pTimeGap));
        assertThat(pTweetPage.remainingGap(), equalTo(pTimeGap.substract(lTweets, 20)));

        Tweet lFirstTweet = lTweets.get(0);
        assertThat(lFirstTweet.getId(), equalTo(349452667745087500L));
        Tweet lLastTweet = lTweets.get(18);
        assertThat(lLastTweet.getId(), equalTo(349443896905965600L));
    }
}
