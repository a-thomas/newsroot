package com.codexperiments.newsroot.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.simpleframework.http.Query;

import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;

import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.test.server.MockBackend;
import com.codexperiments.newsroot.ui.activity.HomeActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity>
{
    private TwitterDatabase mDatabase;
    private MockBackend.Config mServerConfig;
    private MockBackend.Server mServer;

    public HomeActivityTest()
    {
        super(HomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mDatabase = OpenHelperManager.getHelper(getInstrumentation().getContext(), TwitterDatabase.class);
        mDatabase.recreate();

        mServerConfig = mock(MockBackend.Config.class);
    }

    @Override
    protected void tearDown() throws Exception
    {
        if (mServer != null) {
            mServer.stop();
            mServer = null;
        }
        super.tearDown();
    }

    private TwitterManager setupTwitterManagerAuthenticated()
    {
        Application lApplication = (Application) getInstrumentation().getTargetContext().getApplicationContext();
        TwitterManager lTwitterManager = new TwitterManager(lApplication, mDatabase, new TwitterManager.Config() {
            public String getHost()
            {
                return "http://localhost:8378/";
            }

            public String getConsumerKey()
            {
                return "3Ng9QGTB7EpZCHDOIT2jg";
            }

            public String getConsumerSecret()
            {
                return "OolXzfWdSF6uMdgt2mvLNpDl4HOA1JNlN487LvDUA4";
            }

            public String getCallbackURL()
            {
                return "oauth://newsroot-callback";
            }
        });
        return lTwitterManager;
    }

    public void testFindNewTweets_emptyTimeline() throws TwitterAccessException
    {
        // Setup
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_empty.json");
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        // Run
        List<Tweet> lTweets = lTwitterManager.findNewTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(0));
    }

    public void testFindNewTweets_nonEmptyTimeline_noResult() throws TwitterAccessException
    {
        // Setup
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_02.json")
                                                                                    .thenReturn("twitter/tweets_empty.json");
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        // Run
        List<Tweet> lTweets = lTwitterManager.findNewTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));

        // Run
        lTweets = lTwitterManager.findNewTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(0));
    }

    public void testFindNewTweets_nonEmptyTimeline_withResults() throws TwitterAccessException
    {
        // Setup
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_02.json")
                                                                                    .thenReturn("twitter/tweets_03.json");
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        // Run
        List<Tweet> lTweets = lTwitterManager.findNewTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));

        // Run
        lTweets = lTwitterManager.findNewTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));
    }

    public void testFindOldTweets_emptyTimeline() throws TwitterAccessException
    {
        // Setup
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_empty.json");
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        // Run
        List<Tweet> lTweets = lTwitterManager.findNewTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(0));
    }

    public void testFindOldTweets_nonEmptyTimeline_noResult() throws TwitterAccessException
    {
        // Setup
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_03.json")
                                                                                    .thenReturn("twitter/tweets_empty.json");
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        // Run
        List<Tweet> lTweets = lTwitterManager.findOldTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));

        // Run
        lTweets = lTwitterManager.findOldTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(0));
    }

    public void testFindOldTweets_nonEmptyTimeline_withResults() throws TwitterAccessException
    {
        // Setup
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_03.json")
                                                                                    .thenReturn("twitter/tweets_02.json");
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        // Run
        List<Tweet> lTweets = lTwitterManager.findOldTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));

        // Run
        lTweets = lTwitterManager.findOldTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));
    }

    public void testFindTweetsInGap_emptyTimeline() throws TwitterAccessException
    {
    }

    public void testFindTweetsInGap_nonEmptyTimeline_noResult() throws TwitterAccessException
    {
    }

    public void testFindTweetsInGap_nonEmptyTimeline_withResults() throws TwitterAccessException
    {
    }
}
