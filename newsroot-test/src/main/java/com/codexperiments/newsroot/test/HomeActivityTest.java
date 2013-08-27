package com.codexperiments.newsroot.test;

import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.simpleframework.http.Query;

import rx.Observable;
import rx.Observer;
import rx.util.BufferClosing;
import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.codexperiments.newsroot.common.event.AndroidEventBus;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.repository.twitter.TwitterAPI;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.newsroot.test.server.MockBackend;
import com.codexperiments.newsroot.ui.activity.HomeActivity;
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.robolabor.task.android.AndroidTaskManager;
import com.codexperiments.robolabor.task.android.AndroidTaskManagerConfig;
import com.codexperiments.robolabor.task.handler.Task;
import com.codexperiments.robolabor.task.handler.TaskNotifier;

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

    // private TwitterManager setupTwitterManager() {
    // TwitterManager lTwitterManager = new TwitterManager(mApplication, mEventBus, mDatabase, mManagerConfig);
    // return lTwitterManager;
    // }

    // public void testFindNewTweets_noResult_emptyTimeline() throws Exception {
    // Setup
    // mDatabase.executeAssetScript("twitter/ctx_timeline_04.sql", getInstrumentation().getContext());
    // // NewsLoadedEvent.Listener lNewsLoadedEvent = mock(NewsLoadedEvent.Listener.class);
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/ctx_tweet_05.json");
    // TweetListener lTweetListener = mock(TweetListener.class);
    //
    // // mEventBus.registerListener(lNewsLoadedEvent);
    // TwitterManager lTwitterManager = setupTwitterManager();
    // Timeline lTimeline = new Timeline();
    // lTwitterManager.findOlderTweets(lTimeline);
    // lTwitterManager.register(lTweetListener);
    // // Run
    // List<Timeline.Item> lTweets = lTwitterManager.findLatestTweets(lTimeline);
    // lTwitterManager.unregister(lTweetListener);
    // // Check
    // verify(lTweetListener, times(2)).onNewsLoaded(argThat(any(List.class)));
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(0));
    // }

    // public void testFindNewTweets_noResult_emptyTimeline() throws Exception
    // {
    // // Setup
    // mDatabase.executeAssetScript("twitter/ctx_timeline_04.sql");
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/ctx_tweet_empty.json");
    // TwitterManager lTwitterManager = setupTwitterManager();
    // // Run
    // List<Tweet> lTweets = lTwitterManager.findLatestTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(0));
    // }
    //
    // public void testFindNewTweets_noResult_emptyTimeline() throws Exception
    // {
    // // Setup
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/ctx_tweet_empty.json");
    // TwitterManager lTwitterManager = setupTwitterManager();
    // // Run
    // List<Tweet> lTweets = lTwitterManager.findLatestTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(0));
    // }
    //
    // public void testFindNewTweets_noResult_nonEmptyTimeline() throws Exception
    // {
    // // Setup
    // executeSqlScript(getInstrumentation().getContext(), mDatabase.getWritableDatabase(), "twitter/ctx_timeline_04.sql", false);
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/ctx_tweet_empty.json");
    // TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
    // // Run
    // List<Tweet> lTweets = lTwitterManager.findNewTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(0));
    // }
    //
    // public void testFindNewTweets_nonEmptyTimeline_withResults() throws TwitterAccessException
    // {
    // // Setup
    // mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_02.json")
    // .thenReturn("twitter/tweets_03.json");
    // TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
    // // Run
    // List<Tweet> lTweets = lTwitterManager.findNewTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(20));
    //
    // // Run
    // lTweets = lTwitterManager.findNewTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(20));
    // }
    //
    // public void testFindOldTweets_emptyTimeline() throws TwitterAccessException
    // {
    // // Setup
    // mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_empty.json");
    // TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
    // // Run
    // List<Tweet> lTweets = lTwitterManager.findNewTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(0));
    // }
    //
    // public void testFindOldTweets_nonEmptyTimeline_noResult() throws TwitterAccessException
    // {
    // // Setup
    // mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_03.json")
    // .thenReturn("twitter/tweets_empty.json");
    // TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
    // // Run
    // List<Tweet> lTweets = lTwitterManager.findOldTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(20));
    //
    // // Run
    // lTweets = lTwitterManager.findOldTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(0));
    // }
    //
    // public void testFindOldTweets_nonEmptyTimeline_withResults() throws TwitterAccessException
    // {
    // // Setup
    // mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_03.json")
    // .thenReturn("twitter/tweets_02.json");
    // TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
    // // Run
    // List<Tweet> lTweets = lTwitterManager.findOldTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // // assertThat(lTweets.size(), equalTo(20));
    //
    // // Run
    // lTweets = lTwitterManager.findOldTweets();
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(20));
    // }
    //
    // public void testFindTweetsInGap_nonEmptyTimeline_noResult() throws TwitterAccessException, SQLException, IOException
    // {
    // executeSqlScript(getInstrumentation().getContext(), mDatabase.getWritableDatabase(), "ctx_timeline.sql", false);
    // // Setup
    // mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
    // when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_03.json")
    // .thenReturn("twitter/tweets_02.json");
    // TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
    // List<Tweet> lTweets = lTwitterManager.findOldTweets();
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(20));
    //
    // TimeGap lTimeGap = new TimeGap(347536551153791000l, 347417929584889860l);
    // // mDatabase.getTimeGapDao().create(lTimeGap);
    //
    // // Run
    // lTweets = lTwitterManager.findTweetsInGap(lTimeGap);
    // // Check
    // assertThat(lTweets, notNullValue());
    // assertThat(lTweets.size(), equalTo(20));
    // }
    //
    // public void testFindTweetsInGap_nonEmptyTimeline_withResults() throws TwitterAccessException
    // {
    // }

    @SuppressWarnings("unchecked")
    public void testFindNewTweets_nonEmptyTimeline_withResults()
        throws IOException, TwitterAccessException, InterruptedException, Exception
    {
        for (int i = 0; i < 10; ++i) {
            Log.e(getClass().getSimpleName(), "ùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùùù");
            // CountDownLatch lTestFinished = new CountDownLatch(5); // Number of data pages + end of stream event.
            // Setup
            final Observer<Observable<News>> lTweetPageObserver = Mockito.mock(Observer.class, withSettings().verboseLogging());
            final Observer<News> lTweetObserver = Mockito.mock(Observer.class, withSettings().verboseLogging());
            mDatabase.executeAssetScript("twitter/ctx_timeline_01.sql", getInstrumentation().getContext());
            mDatabase.executeAssetScript("twitter/ctx_timeline.sql", getInstrumentation().getContext());
            // TODO Data
            Timeline lTimeline = new Timeline(1, 349497246842241000l, 349443896905965600l);

            // Record
            // countDown(lTestFinished).when(lPageObserver).onNext(argThat(any(BufferClosing.class)));
            // countDown(lTestFinished).when(lPageObserver).onCompleted();
            // countDown(lTestFinished).when(lTweetObserver).onCompleted();

            when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/ctx_tweet_02-1.json")
                                                                                        .thenReturn("twitter/ctx_tweet_02-2.json")
                                                                                        .thenReturn("twitter/ctx_tweet_02-3.json");
            // when(mTwitterManager.query(argThat(any(TwitterQuery.class)))).thenReturn(createParser("twitter/tweets_03.json"));
            // when(mTwitterAPI.getHome(argThat(any(TimeGap.class)), 20)).thenReturn(null);
            // Run
            // List<News> lTweets = mTwitterRepository.findLatestTweets(lTimeline);
            Observable<Observable<News>> lNews = mTwitterRepository.findLatestNews(lTimeline);
            assertThat(lNews, notNullValue());
            lNews.subscribe(new Observer<Observable<News>>() {
                public void onNext(final Observable<News> pNewsPage) {
                    lTweetPageObserver.onNext(pNewsPage);
                    pNewsPage.subscribe(new Observer<News>() {
                        public void onNext(News pNews) {
                            lTweetObserver.onNext(pNews);
                        }
                        
                        public void onCompleted() {
                            lTweetObserver.onCompleted();
                        }

                        public void onError(Throwable pThrowable) {
                            lTweetObserver.onError(pThrowable);
                        }
                    });
                }

                public void onCompleted() {
                    lTweetObserver.onCompleted();
                }

                public void onError(Throwable pE) {
                    lTweetObserver.onError(pE);
                }
            });

            // assertThat(lTestFinished.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS), equalTo(true));
            InOrder lInOrder = Mockito.inOrder(lTweetObserver, lTweetObserver);
            lInOrder.verify(lTweetObserver, times(20)).onNext(argThat(any(Tweet.class)));
            lInOrder.verify(lPageObserver).onNext(argThat(any(BufferClosing.class)));
            lInOrder.verify(lTweetObserver, times(20)).onNext(argThat(any(Tweet.class)));
            lInOrder.verify(lPageObserver).onNext(argThat(any(BufferClosing.class)));
            lInOrder.verify(lTweetObserver, times(19)).onNext(argThat(any(Tweet.class)));
            lInOrder.verify(lPageObserver).onCompleted();
            lInOrder.verify(lTweetObserver).onCompleted();
            lInOrder.verifyNoMoreInteractions();

            Mockito.verify(lTweetObserver, never()).onError(argThat(any(Throwable.class)));

            tearDown();
            setUp();
        }

        mTaskManager.when(new Task<Tweet>() {
            public void onFail(Throwable pArg0) {
            }

            public void onFinish(Tweet pArg0) {
            }

            public Tweet onProcess(TaskNotifier pArg0) throws Exception {
                return null;
            }
        }).then(new Task<Tweet>() {
            public void onFail(Throwable pArg0) {
            }

            public void onFinish(Tweet pArg0) {
            }

            public Tweet onProcess(TaskNotifier pArg0) throws Exception {
                return null;
            }
        });
    }

    public Stubber countDown(final CountDownLatch pCountDownLatch) {
        return doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock pInvocation) throws Throwable {
                // Assert is not sufficient to ensure the latch state (there is a race condition with countDown() instruction).
                // But that should be well enough for a test case.
                assertThat(pCountDownLatch.getCount(), greaterThan(0l));
                pCountDownLatch.countDown();
                return null;
            }
        });
    }
}
