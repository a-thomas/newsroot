package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import android.view.View.OnClickListener;

public interface RxOnListClickEvent<TItem, TView> extends OnClickListener {
    public Observable<Integer> positions();

    public Observable<TView> views();

    public Observable<TItem> items();

    public Subscription subscribe(Observer<Integer> pObserver);

    public Subscription subscribe(Action1<Integer> pAction);
}
