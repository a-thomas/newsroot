package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.util.functions.Func1;
import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.presentation.NewsPresentation;
import com.codexperiments.newsroot.ui.fragment.PageAdapter;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.MoreCallback;

public class RxUI {
    public static final Void VOID_SIGNAL = null;
    public static final Void[] VOID_SIGNALS = new Void[] { null };

    public static <TParam, TResult> Func1<TParam, TResult> toConstant(final TResult pConstant) {
        return new Func1<TParam, TResult>() {
            public TResult call(TParam pParam) {
                return pConstant;
            }
        };
    };

    public static <TValue> Func1<TValue, Boolean> eq(final TValue pConstant) {
        return new Func1<TValue, Boolean>() {
            public Boolean call(TValue pValue) {
                return pConstant.equals(pValue);
            }
        };
    };

    public static Func1<Boolean, Boolean> not() {
        return new Func1<Boolean, Boolean>() {
            public Boolean call(Boolean pValue) {
                if (pValue == Boolean.TRUE) {
                    return Boolean.FALSE;
                } else {
                    return Boolean.TRUE;
                }
            }
        };
    };

    public static <TValue> Observable<TValue> distinct(final Observable<TValue> pObservable) {
        return Observable.create(new OnSubscribeFunc<TValue>() {
            public Subscription onSubscribe(final Observer<? super TValue> pObserver) {
                return pObservable.subscribe(new Observer<TValue>() {
                    TValue mPreviousValue = null;

                    public void onNext(TValue pValue) {
                        if ((pValue != null) && !pValue.equals(mPreviousValue)) {
                            pObserver.onNext(pValue);
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
    };

    public static Observable<Void> fromClick(View view) {
        final PublishSubject<Void> lSubject = PublishSubject.create();
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                lSubject.onNext(null);
            }
        });
        return lSubject;
    }

    public static Observable<Void> fromOnMoreAction(PageAdapter<NewsPresentation> pPageAdapter) {
        final PublishSubject<Void> lPublisher = PublishSubject.create();
        pPageAdapter.setMoreCallback(new MoreCallback() {
            public void onMore() {
                lPublisher.onNext(null);
            }
        });
        return lPublisher;
    }

    public static Observer<Boolean> toDialogVisibleProp(final Dialog pDialog, final Activity pActivity) {
        return new Observer<Boolean>() {
            public void onNext(Boolean pVisible) {
                if (pVisible == Boolean.TRUE) {
                    pDialog.show();
                } else {
                    pDialog.dismiss();
                }
            }

            public void onCompleted() {
                pDialog.dismiss();
            }

            public void onError(Throwable pThrowable) {
                pDialog.dismiss();
                Toast.makeText(pActivity, "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                pThrowable.printStackTrace();
            }
        };
    }

    // public static <TItem> Observer<Page<? extends TItem>> toListView(final PageAdapter<TItem> pAdapter) {
    // return new Observer<Page<? extends TItem>>() {
    // public void onNext(Page<? extends TItem> pPage) {
    // // mTimeline.delete(pTweetPageResponse.initialGap());
    // pAdapter.append(pPage);
    // // pAdapter.insert(new TimeGapPage(pTweetPageResponse.remainingGap()));
    // // mTimeline.add(pTweetPageResponse.tweetPage());
    // // mTimeline.add(pTweetPageResponse.remainingGap());
    // pAdapter.notifyDataSetChanged();
    // }
    //
    // public void onCompleted() {
    // }
    //
    // public void onError(Throwable pThrowable) {
    // }
    // };
    // }
    //
    public static <TItem> Observer<Page<? extends TItem>> toListView(final BaseAdapter pAdapter) {
        return new Observer<Page<? extends TItem>>() {
            public void onNext(Page<? extends TItem> pPage) {
                pAdapter.notifyDataSetChanged();
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
            }
        };
    }

    public static <TItem> Observer<Object> toListView2(final BaseAdapter pAdapter) {
        return new Observer<Object>() {
            public void onNext(Object pValue) {
                pAdapter.notifyDataSetChanged();
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
            }
        };
    }

    public static <TItem> Observer<Page<? extends TItem>> toPageIndex(final PageIndex<TItem> pIndex) {
        return new Observer<Page<? extends TItem>>() {
            public void onNext(Page<? extends TItem> pPage) {
                pIndex.insert(pPage);
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
            }
        };
    }

    public static Observer<Boolean> toActivated(final View pView) {
        return new Observer<Boolean>() {
            public void onNext(Boolean pActivated) {
                pView.setActivated(pActivated.booleanValue());
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
                Log.e("", "", pThrowable);
            }
        };
    }

    public static Observer<Boolean> toEnabled(final View pView) {
        return new Observer<Boolean>() {
            public void onNext(Boolean pActivated) {
                pView.setEnabled(pActivated.booleanValue());
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
            }
        };
    }

    //
    // public static <TItem> Observer<Page<? extends TItem>> toListView(final PageAdapter<TItem> pAdapter,
    // final PublishSubject<Void> pOnFinished)
    // {
    // return new Observer<Page<? extends TItem>>() {
    // public void onNext(Page<? extends TItem> pPage) {
    // // mTimeline.delete(pTweetPageResponse.initialGap());
    // pAdapter.append(pPage);
    // // pAdapter.insert(new TimeGapPage(pTweetPageResponse.remainingGap()));
    // // mTimeline.add(pTweetPageResponse.tweetPage());
    // // mTimeline.add(pTweetPageResponse.remainingGap());
    // pAdapter.notifyDataSetChanged();
    // }
    //
    // public void onCompleted() {
    // pOnFinished.onNext(null);
    // }
    //
    // public void onError(Throwable pThrowable) {
    // pOnFinished.onNext(null);
    // }
    // };
    // }
}
