package com.codexperiments.newsroot.manager.twitter;

import java.io.IOException;
import java.io.InputStream;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public class TwitterDatabase
{
    private static final String DATABASE_NAME = "twitter_database";
    private static final int DATABASE_VERSION = 1;

    private Helper mHelper;
    private SQLiteDatabase mConnection;

    public TwitterDatabase(Application pApplication)
    {
        mHelper = new Helper(pApplication);
        mConnection = mHelper.getWritableDatabase();
    }

    public void close()
    {
        mConnection.close();
    }

    public void recreate()
    {
        try {
            executeScript("sql/twitter_drop.sql");
            mHelper.onCreate(mConnection);
        } catch (IOException eIOException) {
            // TODO Exception handling
            throw new RuntimeException(eIOException);
        }
    }

    public void executeScript(String pAssetPath) throws IOException
    {
        mHelper.executeScript(mConnection, pAssetPath);
    }

    public SQLiteDatabase getConnection()
    {
        return mConnection;
    }


    private static class Helper extends SQLiteOpenHelper
    {
        private Application mApplication;

        public Helper(Application pApplication)
        {
            super(pApplication, DATABASE_NAME, null, DATABASE_VERSION);
            mApplication = pApplication;
        }

        @Override
        public void onCreate(SQLiteDatabase pDatabase)
        {
            try {
                executeScript(pDatabase, "sql/twitter_create.sql");
            } catch (IOException eIOException) {
                // TODO Exception handling
                throw new RuntimeException(eIOException);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase pDatabase, int pOldVersion, int pNewVersion)
        {
            try {
                executeScript(pDatabase, "sql/twitter_drop.sql");
                onCreate(pDatabase);
            } catch (IOException eIOException) {
                // TODO Exception handling
                throw new RuntimeException(eIOException);
            }
        }

        public void executeScript(SQLiteDatabase pDatabase, String pAssetPath) throws IOException
        {
            InputStream lAssetStream = null;
            pDatabase.beginTransaction();
            try {
                lAssetStream = mApplication.getAssets().open(pAssetPath);
                // File can't be more than 2 Go...
                byte[] lScript = new byte[lAssetStream.available()];
                lAssetStream.read(lScript);

                int lPreviousIndex = 0;
                byte lPreviousChar = '\0';
                byte lCurrentChar;
                boolean lIgnore = false;
                int lScriptSize = lScript.length;
                for (int i = 0; i < lScriptSize; ++i) {
                    lCurrentChar = lScript[i];
                    if ((lCurrentChar == '\n' || lCurrentChar == '\r') && (lPreviousChar == ';')) {
                        String lStatement = new String(lScript, lPreviousIndex, (i - lPreviousIndex) + 1, "UTF-8");
                        if (!lIgnore && !TextUtils.isEmpty(lStatement)) {
                            pDatabase.execSQL(lStatement);
                        }

                        lPreviousIndex = i + 1;
                        lIgnore = false;
                    } else if ((lCurrentChar == '-') && (lPreviousChar == '-')) {
                        lIgnore = true;
                    }

                    lPreviousChar = lCurrentChar;
                }
                pDatabase.setTransactionSuccessful();
            } finally {
                pDatabase.endTransaction();
                try {
                    if (lAssetStream != null) lAssetStream.close();
                } catch (IOException eIOException) {
                    Log.e(TwitterDatabase.class.getSimpleName(), "Error while reading assets", eIOException);
                }
            }
        }
    }
}
