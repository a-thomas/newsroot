package com.codexperiments.newsroot.data.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.codexperiments.quickdao.sqlite.SQLiteDatasource;

import java.io.IOException;

/**
 * Proper exception handling in onCreate() and recreate().
 */
public class SqliteTwitterDatabase extends SQLiteDatasource {
    private static final String DATABASE_NAME = "twitter_database";
    private static final int DATABASE_VERSION = 2;

    //region Columns
    public static final String USR_ID = "USR_ID";
    public static final String USR_VERSION = "USR_VERSION";
    public static final String USR_NAME = "USR_NAME";
    public static final String USR_SCREEN_NAME = "USR_SCREEN_NAME";

    public static final String TWT_USR_ID = "TWT_USR_ID";
    public static final String TWT_VERSION = "TWT_VERSION";
    public static final String TWT_TEXT = "TWT_TEXT";
    public static final String TWT_ID = "TWT_ID";
    public static final String TWT_CREATED_AT = "TWT_CREATED_AT";
    //endregion

    //region Tables
    public static final String USR_USER_TABLE = "USR_USER";
    public static final String TWT_TWEET_TABLE = "TWT_TWEET";

    public static final String[] USR_USER_COLUMNS = { USR_ID, USR_VERSION, USR_NAME, USR_SCREEN_NAME };
    public static final String[] TWT_TWEET_COLUMNS = { TWT_USR_ID, TWT_VERSION, TWT_TEXT, TWT_ID, TWT_CREATED_AT };
    //endregion


    public SqliteTwitterDatabase(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        super.onCreate(database);
        try {
            executeScriptFromAssets("sql/create.sql");
        } catch (IOException ioException) {
            // TODO Handle exception
            throw new RuntimeException(ioException);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        super.onUpgrade(database, oldVersion, newVersion);
        try {
            executeScriptFromAssets("sql/drop.sql");
            executeScriptFromAssets("sql/create.sql");
            executeScriptFromAssets("sql/default.sql");
        } catch (IOException ioException) {
            // TODO Handle exception
            throw new RuntimeException(ioException);
        }
    }
}
