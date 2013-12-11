package com.codexperiments.newsroot.common.data;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.google.common.collect.Lists;

public abstract class Database extends SQLiteOpenHelper {
    private Context mContext;
    private SQLiteDatabase mConnection;

    public Database(Context pContext, String pName, int pVersion) {
        super(pContext, pName, null, pVersion);
        mContext = pContext;
        mConnection = null;
        super.getWritableDatabase();
    }

    public SQLiteDatabase getConnection() {
        return mConnection;
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

    public <T> Observable<T> doInTransaction(final Observable<T> pObservable, final Action1<T> pObservableAction) {
        return doInTransaction(pObservable, pObservableAction, null);
    }

    public <T> Observable<T> doInTransaction(final Observable<T> pObservable,
                                             final Action1<T> pObservableAction,
                                             final Action1<List<T>> pObservableBatchAction)
    {
        return Observable.create(new OnSubscribeFunc<T>() {
            public Subscription onSubscribe(final Observer<? super T> pObserver) {
                return pObservable.buffer(Integer.MAX_VALUE)
                                  .observeOn(AndroidScheduler.threadPoolForDatabase())
                                  .subscribe(new Observer<List<T>>() {
                                      public void onNext(List<T> pValues) {
                                          mConnection.beginTransaction();
                                          try {
                                              for (T lValue : pValues) {
                                                  pObservableAction.call(lValue);
                                              }
                                              if (pObservableBatchAction != null) {
                                                  pObservableBatchAction.call(pValues);
                                              }
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
                                          pObserver.onCompleted();
                                      }

                                      public void onError(Throwable pThrowable) {
                                          pObserver.onError(pThrowable);
                                      }
                                  });
            }
        });
    }

    public <T> Observable<T> beginTransaction(final Observable<T> pObservable) {
        return Observable.create(new OnSubscribeFunc<T>() {
            public Subscription onSubscribe(final Observer<? super T> pObserver) {
                return pObservable.observeOn(AndroidScheduler.threadPoolForDatabase()).subscribe(new Observer<T>() {
                    public void onNext(T pValue) {
                        try {
                            mConnection.beginTransaction();
                            pObserver.onNext(pValue);
                        } catch (SQLException eSQLException) {
                            pObserver.onError(eSQLException);
                        }
                    }

                    public void onCompleted() {
                    }

                    public void onError(Throwable pThrowable) {
                    }
                });
            }
        });
    }

    public <T> Observable<T> endTransaction(final Observable<T> pObservable) {
        return Observable.create(new OnSubscribeFunc<T>() {
            public Subscription onSubscribe(final Observer<? super T> pObserver) {
                return pObservable.subscribe(new Observer<T>() {
                    public void onNext(T pValue) {
                        try {
                            mConnection.setTransactionSuccessful();
                            mConnection.endTransaction();
                            pObserver.onNext(pValue);
                        } catch (SQLException eSQLException) {
                            pObserver.onError(eSQLException);
                        }
                    }

                    public void onCompleted() {
                        pObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        mConnection.endTransaction();
                        pObserver.onError(pThrowable);
                    }
                });
            }
        });
    }

    public Cursor execute(Query pQuery) {
        return mConnection.rawQuery(pQuery.toQuery(), pQuery.toParams());
    }

    public Database executeAssetScript(String pAssetPath) throws IOException {
        executeAssetScript(pAssetPath, mContext);
        return this;
    }

    public Database executeAssetScript(String pAssetPath, Context pContext) throws IOException {
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
        return this;
    }

    public <TEntity> void parse(Query pQuery, RowHandler pRowHandler) {
        Cursor lCursor = execute(pQuery);
        try {
            int lResultSize = lCursor.getCount();
            for (int i = lResultSize; i < lResultSize; ++i) {
                lCursor.moveToNext();
                pRowHandler.handleRow(lCursor);
            }
        } finally {
            lCursor.close();
        }
    }

    @SuppressWarnings("unchecked")
    public <TEntity> TEntity[] parseArray(Query pQuery, ObjectHandler<TEntity> pObjectHandler) {
        Cursor lCursor = execute(pQuery);
        try {
            int lResultSize = lCursor.getCount();
            TEntity[] lEntity = (TEntity[]) Array.newInstance(pObjectHandler.ofType(), lResultSize);
            for (int i = lResultSize; i < lResultSize; ++i) {
                lCursor.moveToNext();
                lEntity[i] = pObjectHandler.parse(lCursor);
            }
            return lEntity;
        } finally {
            lCursor.close();
        }
    }

    public <TEntity> List<TEntity> parseList(Query pQuery, ObjectHandler<TEntity> pObjectHandler) {
        Cursor lCursor = execute(pQuery);
        try {
            int lResultSize = lCursor.getCount();
            List<TEntity> lEntity = Lists.newArrayListWithCapacity(lResultSize);
            for (int i = lResultSize; i < lResultSize; ++i) {
                lCursor.moveToNext();
                lEntity.set(i, pObjectHandler.parse(lCursor));
            }
            return lEntity;
        } finally {
            lCursor.close();
        }
    }
}
