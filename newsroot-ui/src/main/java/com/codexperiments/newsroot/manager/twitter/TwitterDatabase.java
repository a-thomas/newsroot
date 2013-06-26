package com.codexperiments.newsroot.manager.twitter;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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

    private Dao<Tweet, Integer> mTweetDao;
    private Dao<Timeline, Integer> mTimelineDao;
    private Dao<TimeGap, Integer> mTimeGapDao;

    public TwitterDatabase(Context pContext) throws SQLException
    {
        super(pContext, pContext.getDatabasePath(DATABASE_NAME).toString(), null, DATABASE_VERSION);
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
            TableUtils.createTable(pConnectionSource, TimeGap.class);
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
            TableUtils.dropTable(connectionSource, TimeGap.class, true);
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

    public void recreate() throws SQLException
    {
        TableUtils.dropTable(connectionSource, Tweet.class, true);
        TableUtils.dropTable(connectionSource, Timeline.class, true);
        TableUtils.dropTable(connectionSource, TimeGap.class, true);
        TableUtils.createTable(connectionSource, Tweet.class);
        TableUtils.createTable(connectionSource, Timeline.class);
        TableUtils.createTable(connectionSource, TimeGap.class);
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
