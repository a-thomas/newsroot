package com.codexperiments.newsroot.manager.twitter;

import java.io.File;
import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class TwitterDatabase extends OrmLiteSqliteOpenHelper
{
    private static final String DATABASE_NAME = "twitter_database";
    private static final int DATABASE_VERSION = 1;

    private File mDatabasePath;
    private Dao<Tweet, Integer> mTweetDao;
    private Dao<Timeline, Integer> mTimelineDao;
    private Dao<TimeGap, Integer> mTimeGapDao;

    public static File getDatabasePath(Context pContext)
    {
        // Cf. http://developer.android.com/guide/topics/data/data-storage.html#filesExternalits for the reason we use this dir.
        String applicationPackage = pContext.getPackageName();
        String separator = File.pathSeparator;
        String applicationDirectory = "Android" + separator + "data/" + separator + applicationPackage + separator + "files";
        return new File(new File(Environment.getExternalStorageDirectory(), applicationDirectory), DATABASE_NAME);
    }

    public TwitterDatabase(Context pContext) throws SQLException
    {
        super(pContext, getDatabasePath(pContext).toString(), null, DATABASE_VERSION);
        mDatabasePath = getDatabasePath(pContext);
        mTweetDao = getDao(Tweet.class);
        mTimelineDao = getDao(Timeline.class);
        mTimeGapDao = getDao(TimeGap.class);
    }

    @Override
    public void onCreate(SQLiteDatabase pDatabase, ConnectionSource pConnectionSource)
    {
        try {
            TableUtils.createTable(pConnectionSource, Tweet.class);
            TableUtils.createTable(pConnectionSource, Timeline.class);
        } catch (SQLException eSQLException) {
            throw new RuntimeException(eSQLException);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        try {
            TableUtils.dropTable(connectionSource, Tweet.class, true);
            TableUtils.dropTable(connectionSource, Timeline.class, true);
            // after we drop the old database, we create the new one
            onCreate(db, connectionSource);
        } catch (SQLException eSQLException) {
            throw new RuntimeException(eSQLException);
        }
    }

    @Override
    public void close()
    {
        super.close();
        mTweetDao = null;
        mTimelineDao = null;
        mTimeGapDao = null;
    }

    public void delete()
    {
        close();
        mDatabasePath.delete();
    }

    public Dao<Tweet, Integer> getTweetDao()
    {
        return mTweetDao;
    }

    public Dao<Timeline, Integer> getTimelineDao()
    {
        return mTimelineDao;
    }

    public Dao<TimeGap, Integer> getTimeGapDao()
    {
        return mTimeGapDao;
    }
}
