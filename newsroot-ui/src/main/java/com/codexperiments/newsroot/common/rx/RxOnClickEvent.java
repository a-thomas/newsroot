package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import android.view.View.OnClickListener;

public interface RxOnClickEvent extends OnClickListener {
    public Observable<Void> toObservable();

    public Subscription subscribe(Observer<Void> pObserver);

    public Subscription subscribe(Action1<Void> pAction);
}
