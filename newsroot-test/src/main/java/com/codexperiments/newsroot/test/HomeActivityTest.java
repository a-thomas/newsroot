package com.codexperiments.newsroot.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;

import org.simpleframework.http.Query;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
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
        if (mDatabase == null) {
            mDatabase = OpenHelperManager.getHelper(getInstrumentation().getContext(), TwitterDatabase.class);
            // mDatabase.getWritableDatabase();
        }
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

    /**
     * Executes the given SQL asset in the given database (SQL file should be UTF-8). The database file may contain multiple SQL
     * statements. Statements are split using a simple regular expression (something like "semicolon before a line break"), not by
     * analyzing the SQL syntax. This will work for many SQL files, but check yours.
     * 
     * @return number of statements executed.
     */
    public static int executeSqlScript(Context context, SQLiteDatabase db, String assetFilename, boolean transactional)
                    throws IOException
    {
        byte[] bytes = readAssetToByte(context.getAssets(), assetFilename);
        String sql = new String(bytes, "UTF-8");
        String[] lines = sql.split(";(\\s)*[\n\r]");
        int count;
        if (transactional) {
            count = executeSqlStatementsInTx(db, lines);
        } else {
            count = executeSqlStatements(db, lines);
        }
        Log.i(HomeActivityTest.class.getSimpleName(), "Executed " + count + " statements from SQL script '" + assetFilename + "'");
        return count;
    }

    public static int executeSqlStatementsInTx(SQLiteDatabase db, String[] statements)
    {
        db.beginTransaction();
        try {
            int count = executeSqlStatements(db, statements);
            db.setTransactionSuccessful();
            return count;
        } finally {
            db.endTransaction();
        }
    }

    public static int executeSqlStatements(SQLiteDatabase db, String[] statements)
    {
        int count = 0;
        for (String line : statements) {
            line = line.trim();
            if (line.length() > 0) {
                db.execSQL(line);
                count++;
            }
        }
        return count;
    }

    public static byte[] readAssetToByte(AssetManager assetManager, String pAssetPath) throws IOException
    {
        InputStream lInput = null;
        try {
            lInput = assetManager.open(pAssetPath);
            // File can't be more than 2 Go...
            byte[] lInputBuffer = new byte[lInput.available()];
            lInput.read(lInputBuffer);
            return lInputBuffer;
        } finally {
            try {
                if (lInput != null) lInput.close();
            } catch (IOException ioException) {
                Log.e(MockBackend.class.getSimpleName(), "Error while reading assets", ioException);
            }
        }
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
        // assertThat(lTweets.size(), equalTo(20));

        // Run
        lTweets = lTwitterManager.findOldTweets();
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));
    }

    public void testFindTweetsInGap_nonEmptyTimeline_noResult() throws TwitterAccessException, SQLException, IOException
    {
        executeSqlScript(getInstrumentation().getContext(), mDatabase.getWritableDatabase(), "ctx_timeline.sql", false);
        // Setup
        mServer = new MockBackend.Server(getInstrumentation().getContext(), mServerConfig);
        when(mServerConfig.getResponseAsset(argThat(any(Query.class)), anyString())).thenReturn("twitter/tweets_03.json")
                                                                                    .thenReturn("twitter/tweets_02.json");
        TwitterManager lTwitterManager = setupTwitterManagerAuthenticated();
        List<Tweet> lTweets = lTwitterManager.findOldTweets();
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));

        TimeGap lTimeGap = new TimeGap(347536551153791000l, 347417929584889860l);
        mDatabase.getTimeGapDao().create(lTimeGap);

        // Run
        lTweets = lTwitterManager.findTweetsInGap(lTimeGap);
        // Check
        assertThat(lTweets, notNullValue());
        assertThat(lTweets.size(), equalTo(20));
    }

    public void testFindTweetsInGap_nonEmptyTimeline_withResults() throws TwitterAccessException
    {
    }
}
