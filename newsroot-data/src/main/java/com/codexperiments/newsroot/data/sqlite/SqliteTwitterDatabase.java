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
