package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.util.functions.Func1;

import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class AsyncCommand<TParam, TResult> extends Observable<TResult> implements Observer<TParam> {
    // ScheduledSubject<Exception> exceptions;
    private PublishSubject<TParam> mCommand = PublishSubject.create();
    private PublishSubject<Boolean> mRunning = PublishSubject.create(); // TODO Lazy
    private Observable<TResult> mResult;
    private Scheduler mScheduler = AndroidScheduler.threadForUI();

    public static <TParam, TResult> AsyncCommand<TParam, TResult> create(Func1<TParam, Observable<TResult>> pAsyncCommand) {
        return new AsyncCommand<TParam, TResult>(pAsyncCommand);
    }

    protected AsyncCommand(Func1<TParam, Observable<TResult>> pAsyncCommand) {
        super(null);
        mCommand = PublishSubject.create();
        merge(mCommand.map(pAsyncCommand));
        mResult = Observable.merge(mCommand.map(pAsyncCommand)).observeOn(mScheduler);
    }

    // protected Observable<TResult> merge(Observable<Observable<TResult>> pCommand) {
    // return Observable.create(new OnSubscribeFunc<TResult>() {
    // private final MergeSubscription ourSubscription = new MergeSubscription();
    //
    // public Subscription onSubscribe(Observer<? super TResult> pResult) {
    // SafeObservableSubscription subscription = new SafeObservableSubscription(ourSubscription);
    // return;
    // }
    // });
    // }

    // protected Observable<TResult> perform(Observable<TResult> pCommand) {
    // return pCommand.map(new Func1<TResult, TResult>() {
    // public TResult call(TResult pResult) {
    // mRunning.onNext(Boolean.FALSE);
    // return pResult;
    // }
    // });
    // }

    // public void execute(TParam pParam) {
    // mInflight.onNext(Boolean.TRUE);
    // mCommand.onNext(pParam);
    // mInflight.onNext(Boolean.FALSE);
    // }

    // public Observable<TResult> result() {
    // return mCommand;
    // }

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
