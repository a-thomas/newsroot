package com.codexperiments.newsroot.test;

import com.codexperiments.newsroot.manager.twitter.TwitterAccessException;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.test.common.TestCase;
import com.codexperiments.newsroot.ui.activity.HomeActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class HomeActivityTest extends TestCase<HomeActivity>
{
    private TwitterDatabase mDatabase;

    public HomeActivityTest()
    {
        super(HomeActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mDatabase = OpenHelperManager.getHelper(getApplication(), TwitterDatabase.class);
        mDatabase.delete();
        mDatabase = OpenHelperManager.getHelper(getApplication(), TwitterDatabase.class);
    }

    private TwitterManager setupTwitterManagerAuthenticated()
    {
        TwitterManager lTwitterManager = new TwitterManager(getApplication(), mDatabase, new TwitterManager.Config() {
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
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        lTwitterManager.findNewTweets();
    }

    public void testFindNewTweets_nonEmptyTimeline_noResult() throws TwitterAccessException
    {
    }

    public void testFindNewTweets_nonEmptyTimeline_withResults() throws TwitterAccessException
    {
    }

    public void testFindOldTweets_emptyTimeline() throws TwitterAccessException
    {
    }

    public void testFindOldTweets_nonEmptyTimeline_noResult() throws TwitterAccessException
    {
    }

    public void testFindOldTweets_nonEmptyTimeline_withResults() throws TwitterAccessException
    {
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
