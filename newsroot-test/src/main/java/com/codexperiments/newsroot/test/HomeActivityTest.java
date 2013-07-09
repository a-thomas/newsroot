package com.codexperiments.newsroot.test;

import static org.mockito.Mockito.mock;
import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;

import com.codexperiments.newsroot.common.event.AndroidEventBus;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.test.server.MockBackend;
import com.codexperiments.newsroot.ui.activity.HomeActivity;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {
    private Application mApplication;
    private TwitterDatabase mDatabase;
    private MockBackend.Config mServerConfig;
    private MockBackend.Server mServer;
    private EventBus mEventBus;

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
    }

    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mServer.stop();
        super.tearDown();
    }

    private TwitterManager setupTwitterManager() {
        Application lApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        TwitterManager lTwitterManager = new TwitterManager(lApplication, mEventBus, mDatabase, new TwitterManager.Config() {
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
        return lTwitterManager;
    }

    public void testFindNewTweets_noResult_emptyTimeline() throws Exception {
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
    }
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
}
