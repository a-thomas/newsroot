package com.codexperiments.rx;

import java.lang.reflect.Array;
import java.util.List;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.codexperiments.newsroot.common.data.ObjectHandler;
import com.codexperiments.newsroot.common.data.Query;
import com.codexperiments.newsroot.common.data.Table;

public class RxAndroid {
    public static <TResult> Observable<TResult> from(Observable<TResult> pFindTweets, Fragment pFragment) {
        return Observable.create(new OnSubcribeFragmentSupport<TResult>(pFindTweets, pFragment));
    }

    private static class OnSubcribeFragmentSupport<TResult> implements OnSubscribeFunc<TResult> {
        private final Observable<TResult> mSource;
        private Fragment mFragment;

        public OnSubcribeFragmentSupport(Observable<TResult> pSource, Fragment pFragment) {
            super();
            mSource = pSource;
            mFragment = pFragment;
        }

        public Subscription onSubscribe(final Observer<? super TResult> pObserver) {
            return mSource.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<TResult>() {
                public void onNext(TResult pResult) {
                    pObserver.onNext(pResult);
                }

                public void onCompleted() {
                    pObserver.onCompleted();
                }

                public void onError(Throwable pError) {
                    pObserver.onError(pError);
                }
            });
        }
    }

    public static <TTable extends Enum<?> & Table> Observable<Cursor> select(final SQLiteDatabase pDatabase,
                                                                             final Observable<Query<TTable>> pQueries)
    {
        return Observable.create(new OnSubscribeFunc<Cursor>() {
            public Subscription onSubscribe(final Observer<? super Cursor> pObserver) {
                return pQueries.observeOn(AndroidScheduler.threadPoolForDatabase()).subscribe(new Observer<Query<TTable>>() {
                    public void onNext(Query<TTable> pQuery) {
                        Cursor lCursor = pDatabase.rawQuery(pQuery.toQuery(), pQuery.toParams());
                        pObserver.onNext(lCursor);
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

    public static <TEntity> Observable<TEntity[]> asArray(final Observable<Cursor> pCursors,
                                                          final ObjectHandler<TEntity> pObjectHandler)
    {
        return pCursors.map(new Func1<Cursor, TEntity[]>() {
            @SuppressWarnings("unchecked")
            public TEntity[] call(Cursor pCursor) {
                try {
                    int lResultSize = pCursor.getCount();
                    pObjectHandler.initialize(pCursor);
                    TEntity[] lEntity = (TEntity[]) Array.newInstance(pObjectHandler.ofType(), lResultSize);
                    for (int i = 0; i < lResultSize; ++i) {
                        pCursor.moveToNext();
                        lEntity[i] = pObjectHandler.parse(pCursor);
                    }
                    return lEntity;
                } finally {
                    pCursor.close();
                }
            }
        });
    }

    public static <T> Observable<T> doInTransaction(final SQLiteDatabase pDatabase,
                                                    final Observable<T> pObservable,
                                                    final Action1<T> pObservableAction)
    {
        return doInTransaction(pDatabase, pObservable, pObservableAction, null);
    }

    public static <T> Observable<T> doInTransaction(final SQLiteDatabase pDatabase,
                                                    final Observable<T> pObservable,
                                                    final Action1<T> pObservableAction,
                                                    final Action1<List<T>> pObservableBatchAction)
    {
        return Observable.create(new OnSubscribeFunc<T>() {
            public Subscription onSubscribe(final Observer<? super T> pObserver) {
                return pObservable.buffer(Integer.MAX_VALUE)
                                  .observeOn(AndroidScheduler.threadPoolForDatabase())
                                  .subscribe(new Observer<List<T>>() {
                                      public void onNext(List<T> pValues) {
                                          pDatabase.beginTransaction();
                                          try {
                                              for (T lValue : pValues) {
                                                  pObservableAction.call(lValue);
                                              }
                                              if (pObservableBatchAction != null) {
                                                  pObservableBatchAction.call(pValues);
                                              }
                                              pDatabase.setTransactionSuccessful();
                                              pDatabase.endTransaction();

                                              // Once data is committed, push data further into the pipeline.
                                              for (T value : pValues) {
                                                  pObserver.onNext(value);
                                              }
                                          } catch (SQLException eSQLException) {
                                              pDatabase.endTransaction();
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

    public static <T> Observable<T> beginTransaction(final SQLiteDatabase pDatabase, final Observable<T> pObservable) {
        return Observable.create(new OnSubscribeFunc<T>() {
            public Subscription onSubscribe(final Observer<? super T> pObserver) {
                return pObservable.observeOn(AndroidScheduler.threadPoolForDatabase()).subscribe(new Observer<T>() {
                    public void onNext(T pValue) {
                        try {
                            pDatabase.beginTransaction();
                            pObserver.onNext(pValue);
                        } catch (SQLException eSQLException) {
                            pObserver.onError(eSQLException);
                        }
                    }

                    public void onCompleted() {
                        pObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        Log.e(getClass().getSimpleName(), "Ending transaction", pThrowable);
                        pObserver.onError(pThrowable);
                    }
                });
            }
        });
    }

    public static <T> Observable<T> endTransaction(final SQLiteDatabase pDatabase, final Observable<T> pObservable) {
        return Observable.create(new OnSubscribeFunc<T>() {
            public Subscription onSubscribe(final Observer<? super T> pObserver) {
                return pObservable.subscribe(new Observer<T>() {
                    public void onNext(T pValue) {
                        try {
                            pDatabase.setTransactionSuccessful();
                            pDatabase.endTransaction();
                            pObserver.onNext(pValue);
                        } catch (SQLException eSQLException) {
                            pObserver.onError(eSQLException);
                        }
                    }

                    public void onCompleted() {
                        pObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        Log.e(getClass().getSimpleName(), "Ending transaction", pThrowable);
                        if (pDatabase.inTransaction()) {
                            pDatabase.endTransaction();
                        }
                        pObserver.onError(pThrowable);
                    }
                });
            }
        });
    }
}
