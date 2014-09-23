package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import android.view.View.OnClickListener;
import rx.functions.Action1;

public interface RxOnClickEvent extends OnClickListener {
    public Observable<Void> toObservable();

    public Subscription subscribe(Observer<Void> pObserver);

    public Subscription subscribe(Action1<Void> pAction);
}
