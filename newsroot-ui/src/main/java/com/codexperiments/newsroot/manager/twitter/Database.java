package com.codexperiments.newsroot.manager.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.app.Application;
import android.content.Context;
import android.database.SQLException;
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

    public <T> Observable<T> doInTransaction(final Observable<T> pObservable,
                                             final Action1<T> pObservableAction,
                                             final Action1<List<T>> pObservableBatchAction)
    {
        return Observable.create(new Func1<Observer<T>, Subscription>() {
            public Subscription call(final Observer<T> pObserver) {
                return pObservable.buffer(pObservable.controller()).subscribe(new Observer<List<T>>() {
                    public void onNext(List<T> pValues) {
                        Log.e(getClass().getSimpleName(), "XXXXXXXXXXXXXXXX" + pValues.size());
                        mConnection.beginTransaction();
                        try {
                            for (T lValue : pValues) {
                                pObservableAction.call(lValue);
                            }
                            pObservableBatchAction.call(pValues);
                            mConnection.setTransactionSuccessful();
                            mConnection.endTransaction();

                            // Once data is committed, push data further into the pipeline.
                            for (T value : pValues) {
                                pObserver.onNext(value);
                            }
                        } catch (SQLException eSQLException) {
                            mConnection.endTransaction();
                            pObserver.onError(eSQLException);
                            throw eSQLException;
                        }
                    }

                    public void onCompleted() {
                        Log.e(getClass().getSimpleName(), "YYYYYYYYYY");
                        pObserver.onCompleted();
                    }

                    public void onError(Throwable pException) {
                        pObserver.onError(pException);
                    }
                });
            }
        });
    }

    // public <TResult> void executeInTransaction(Callable<TResult> pCallable) throws Exception {
    // mConnection.beginTransaction();
    // try {
    // pCallable.call();
    // mConnection.setTransactionSuccessful();
    // } finally {
    // mConnection.endTransaction();
    // }
    // }
    //
    // public void executeInTransaction(Runnable pRunnable) throws Exception {
    // mConnection.beginTransaction();
    // try {
    // pRunnable.run();
    // mConnection.setTransactionSuccessful();
    // } finally {
    // mConnection.endTransaction();
    // }
    // }

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
