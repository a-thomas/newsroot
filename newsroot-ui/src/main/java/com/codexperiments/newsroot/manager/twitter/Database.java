package com.codexperiments.newsroot.manager.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import android.app.Application;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

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

    // public <T> void apply(final Observable<T> pObservable) {
    // pObservable.subscribe(new Observer<T>() {
    // public void onNext(T pT) {
    // }
    //
    // public void onCompleted() {
    // }
    //
    // public void onError(Throwable pThrowable) {
    // }
    // });
    // }
    //
    // public <T, TPage extends Iterable<T>> void run(final Observable<T> pObservable,
    // final TPage pPage,
    // final Observer<TPage> pPagedObserver)
    // {
    // pObservable.subscribe(new Observer<T>() {
    // public void onNext(T pT) {
    // }
    //
    // public void onCompleted() {
    // pPagedObserver.onNext(pPage);
    // }
    //
    // public void onError(Throwable pThrowable) {
    // pPagedObserver.onError(pThrowable);
    // }
    // });
    // }
    // new Action1<T>() {
    // public void call(T pValue) {
    // try {
    // mConnection.beginTransaction();
    //
    // // Once transaction is started, push data further into the pipeline.
    // pObserver.onNext(pValue);
    // } catch (SQLException eSQLException) {
    // mConnection.endTransaction();
    // pObserver.onError(eSQLException);
    // throw eSQLException;
    // }
    // }
    // }
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

    // public <T> Observable<T> beginTransaction(final Observable<T> pObservable) {
    // return Observable.create(new OnSubscribeFunc<T>() {
    // public Subscription onSubscribe(final Observer<? super T> pObserver) {
    // return pObservable.buffer(Integer.MAX_VALUE)
    // .observeOn(AndroidScheduler.threadPoolForDatabase())
    // .subscribe(new Action1<List<T>>() {
    // public void call(List<T> pValues) {
    // try {
    // mConnection.beginTransaction();
    //
    // // Once transaction is started, push data further into the pipeline.
    // for (T value : pValues) {
    // pObserver.onNext(value);
    // }
    // pObserver.onCompleted();
    // } catch (SQLException eSQLException) {
    // mConnection.endTransaction();
    // pObserver.onError(eSQLException);
    // throw eSQLException;
    // }
    // }
    // });
    // }
    // });
    // }

    // public <T> Observable<T> endTransaction(final Observable<T> pObservable) {
    // return Observable.create(new OnSubscribeFunc<T>() {
    // public Subscription onSubscribe(final Observer<? super T> pObserver) {
    // return pObservable.buffer(Integer.MAX_VALUE).subscribe(new Action1<List<T>>() {
    // public void call(List<T> pValues) {
    // try {
    // mConnection.setTransactionSuccessful();
    // mConnection.endTransaction();
    //
    // // Once data is committed, push data further into the pipeline.
    // for (T value : pValues) {
    // pObserver.onNext(value);
    // }
    // pObserver.onCompleted();
    // } catch (SQLException eSQLException) {
    // mConnection.endTransaction();
    // pObserver.onError(eSQLException);
    // }
    // }
    // });
    // }
    // });
    // }

    public Database executeAssetScript(String pAssetPath) throws IOException {
        executeAssetScript(pAssetPath, mApplication);
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
}
