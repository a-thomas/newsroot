package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import rx.util.functions.Func2;
import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.ui.fragment.PageAdapter;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.MoreCallback;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.RxRecycleCallback;

public class RxUI {
    public static final Void VOID_SIGNAL = null;
    public static final Void[] VOID_SIGNALS = new Void[] { null };

    public static Observer<Boolean> toActivated(final Observable<? extends View> pViews) {
        PublishSubject<Boolean> lProperty = PublishSubject.create();
        Observable.zip(lProperty, pViews, new Func2<Boolean, View, Boolean>() {
            public Boolean call(Boolean pActivated, View pView) {
                if (pView != null) {
                    pView.setActivated(pActivated.booleanValue());
                }
                return pActivated;
            }
        }).subscribe(new Action1<Object>() {
            public void call(Object pT1) {
            }
        });
        return lProperty;
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

    public static RxOnClickEvent fromClick() {
        return new RxOnClickEvent() {
            PublishSubject<Void> lSubject = PublishSubject.create();

            public void onClick(View view) {
                lSubject.onNext(null);
            }

            public Observable<Void> toObservable() {
                return lSubject;
            }

            public Subscription subscribe(Observer<Void> pObserver) {
                return lSubject.subscribe(pObserver);
            }

            public Subscription subscribe(Action1<Void> pAction) {
                return lSubject.subscribe(pAction);
            }
        };
    }

    public static <TItem> RxOnListClickEvent<TItem> fromListClick(final ListView pListView) {
        return new RxOnListClickEvent<TItem>() {
            private PublishSubject<Integer> mSubject = PublishSubject.create();

            @Override
            public void onClick(View pView) {
                int lPosition = pListView.getPositionForView(pView);
                if (lPosition != AdapterView.INVALID_POSITION) {
                    mSubject.onNext(Integer.valueOf(lPosition));
                }
            }

            public Observable<Integer> positions() {
                return mSubject;
            }

            public Observable<TItem> items() {
                return mSubject.map(new Func1<Integer, TItem>() {
                    @SuppressWarnings("unchecked")
                    public TItem call(Integer pPosition) {
                        return (TItem) pListView.getItemAtPosition(pPosition);
                    }
                });
            }

            public Subscription subscribe(Observer<Integer> pObserver) {
                return mSubject.subscribe(pObserver);
            }

            public Subscription subscribe(Action1<Integer> pObserver) {
                return mSubject.subscribe(pObserver);
            }
        };
    }

    public static <TItem> RxRecycleCallback<TItem> fromRecycleListItem(Class<TItem> pClass) {
        return new RxRecycleCallback<TItem>() {
            private PublishSubject<TItem> mSubjectItems = PublishSubject.create();
            private PublishSubject<View> mSubjectViews = PublishSubject.create();

            @Override
            public void onRecycle(TItem pItem, View pView) {
                mSubjectItems.onNext(pItem);
                mSubjectViews.onNext(pView);
            }

            public Subscription subscribe(Observer<TItem> pObserver) {
                return mSubjectItems.subscribe(pObserver);
            }

            public Subscription subscribe(Action1<TItem> pObserver) {
                return mSubjectItems.subscribe(pObserver);
            }

            @Override
            public Observable<TItem> toItems() {
                return mSubjectItems;
            }

            @Override
            public Observable<View> toViews() {
                return mSubjectViews;
            }
        };
    }

    public static Observer<Boolean> toItem(final ListView pListView) {
        return new Observer<Boolean>() {
            public void onNext(Boolean pActivated) {
                View lItemView = findItem(null);
                if (lItemView != null) {
                    lItemView.setActivated(pActivated.booleanValue());
                }
            }

            private View findItem(Object pItem) {
                int lFirstPosition = pListView.getFirstVisiblePosition();
                int lLastPosition = pListView.getLastVisiblePosition();
                for (int lPosition = lFirstPosition; lPosition <= lLastPosition; ++lPosition) {
                    if (pItem == pListView.getItemAtPosition(lPosition)) {
                        return pListView.getChildAt(lPosition - lFirstPosition);
                    }
                }
                return null;
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
                Log.e("", "", pThrowable);
            }
        };
    }

    public static <TItem, TView> Func1<TItem, TView> toListItem(final ListView pListView, final Class<TView> pClass) {
        return new Func1<TItem, TView>() {
            @SuppressWarnings("unchecked")
            public TView call(TItem pItem) {
                int lFirstPosition = pListView.getFirstVisiblePosition();
                int lLastPosition = pListView.getLastVisiblePosition();
                for (int lPosition = lFirstPosition; lPosition <= lLastPosition; ++lPosition) {
                    if (pItem == pListView.getItemAtPosition(lPosition)) {
                        TView lView = (TView) pListView.getChildAt(lPosition - lFirstPosition);
                        // This check is just a security
                        if (lView.getClass() == pClass) {
                            return lView;
                        }
                    }
                }
                return null;
            }
        };
    }

    public static Observable<Void> fromOnMoreAction(PageAdapter<?> pPageAdapter) {
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

    public static <TView extends View> Func1<TView, TView> self() {
        return new Func1<TView, TView>() {
            public TView call(TView pView) {
                return pView;
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

    public static Observer<Boolean> toDisabled(final View pView) {
        return new Observer<Boolean>() {
            public void onNext(Boolean pActivated) {
                pView.setEnabled(!pActivated.booleanValue());
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
