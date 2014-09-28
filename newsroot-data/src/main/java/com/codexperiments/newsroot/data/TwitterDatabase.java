package com.codexperiments.newsroot.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.codexperiments.quickdao.Database;

import java.io.IOException;

/**
 * Proper exception handling in onCreate() and recreate().
 */
public class TwitterDatabase extends Database {
    private static final String DATABASE_NAME = "twitter_database";
    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase connection;

    public TwitterDatabase(Context context) {
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
}
