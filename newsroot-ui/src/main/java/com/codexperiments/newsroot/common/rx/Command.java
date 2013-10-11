package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.util.functions.Func1;

public class Command<TParam, TResult> extends Observable<TResult> implements Observer<TParam> {
    private PublishSubject<TParam> mCommand = PublishSubject.create();
    private Observable<TResult> mResult;

    public static <TParam, TResult> Command<TParam, TResult> create(Func1<TParam, TResult> pCommand) {
        return new Command<TParam, TResult>(pCommand);
    }

    public static <TParam, TResult> Command<TParam, TParam> create() {
        // TODO Seems inefficient.
        return new Command<TParam, TParam>(new Func1<TParam, TParam>() {
            public TParam call(TParam pParam) {
                return pParam;
            }
        });
    }

    protected Command(Func1<TParam, TResult> pCommand) {
        super(null);
        mCommand = PublishSubject.create();
        mResult = mCommand.map(pCommand);
    }

    @Override
    public Subscription subscribe(Observer<? super TResult> pObserver) {
        return mResult.subscribe(pObserver);
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
