package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import android.view.View.OnClickListener;

public interface RxOnListClickEvent<TItem> extends OnClickListener {
    public Observable<Integer> positions();

    public Observable<TItem> items();

    public Subscription subscribe(Observer<Integer> pObserver);

    public Subscription subscribe(Action1<Integer> pAction);
}
