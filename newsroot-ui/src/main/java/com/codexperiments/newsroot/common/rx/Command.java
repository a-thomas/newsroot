package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class Command<TParam> extends Observable<TParam> implements Observer<TParam> {
    private PublishSubject<TParam> mCommand = PublishSubject.create();

    public static <TParam> Command<TParam> create() {
        return new Command<TParam>();
    }

    protected Command() {
        super(null);
        mCommand = PublishSubject.create();
    }

    @Override
    public Subscription subscribe(Observer<? super TParam> pObserver) {
        return mCommand.subscribe(pObserver);
    }

    @Override
    public void onNext(TParam pParam) {
        mCommand.onNext(pParam);
    }

    @Override
    public void onCompleted() {
        mCommand.onCompleted();
    }

    @Override
    public void onError(Throwable pThrowable) {
        mCommand.onError(pThrowable);
    }
}
