package com.codexperiments.newsroot.manager.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

public abstract class Database extends SQLiteOpenHelper {
    private Application mApplication;
    private SQLiteDatabase mConnection;

    public Database(Application pApplication, String pName, int pVersion) {
        super(pApplication, pName, null, pVersion);
        mApplication = pApplication;
        mConnection = null;
        super.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase pDatabase) {
        mConnection = pDatabase;
    }

    @Override
    public void onUpgrade(SQLiteDatabase pDatabase, int pOldVersion, int pNewVersion) {
        mConnection = pDatabase;
        recreate();
    }

    public void onDestroy(SQLiteDatabase pDatabase) {
        mConnection = pDatabase;
    }

    public void recreate() {
        onDestroy(getWritableDatabase());
        onCreate(getWritableDatabase());
    }

    public <TResult> void executeInTransaction(Callable<TResult> pCallable) throws Exception {
        mConnection.beginTransaction();
        try {
            pCallable.call();
            mConnection.setTransactionSuccessful();
        } finally {
            mConnection.endTransaction();
        }
    }

    public void executeInTransaction(Runnable pRunnable) throws Exception {
        mConnection.beginTransaction();
        try {
            pRunnable.run();
            mConnection.setTransactionSuccessful();
        } finally {
            mConnection.endTransaction();
        }
    }

    public void executeAssetScript(String pAssetPath) throws IOException {
        executeAssetScript(pAssetPath, mApplication);
    }

    public void executeAssetScript(String pAssetPath, Context pContext) throws IOException {
        InputStream lAssetStream = null;
        mConnection.beginTransaction();
        try {
            lAssetStream = pContext.getAssets().open(pAssetPath);
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
                        mConnection.execSQL(lStatement);
                    }

                    lPreviousIndex = i + 1;
                    lIgnore = false;
                } else if ((lCurrentChar == '-') && (lPreviousChar == '-')) {
                    lIgnore = true;
                }

                lPreviousChar = lCurrentChar;
            }
            mConnection.setTransactionSuccessful();
        } finally {
            mConnection.endTransaction();
            try {
                if (lAssetStream != null) lAssetStream.close();
            } catch (IOException eIOException) {
                Log.e(Database.class.getSimpleName(), "Error while reading assets", eIOException);
            }
        }
    }
}
