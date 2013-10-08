package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import android.app.Activity;
import android.app.Dialog;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.ui.fragment.PageAdapter;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.MoreCallback;

public class RxUI {
    public static final Void VOID_SIGNAL = null;
    public static final Void[] VOID_SIGNALS = new Void[] { null };

    public static Observable<Void> fromOnMoreAction(PageAdapter<News> pPageAdapter) {
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
    public static <TItem> Observer<Page<? extends TItem>> notifyListView(final BaseAdapter pAdapter) {
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
