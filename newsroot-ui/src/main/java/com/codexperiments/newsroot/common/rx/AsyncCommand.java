package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.util.functions.Action0;
import rx.util.functions.Func1;

import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class AsyncCommand<TParam, TResult> extends Observable<TResult> implements Observer<TParam> {
    // ScheduledSubject<Exception> exceptions;
    private PublishSubject<TParam> mCommand = PublishSubject.create();
    private PublishSubject<TParam> mBeforeExecute = PublishSubject.create(); // TODO Lazy
    private PublishSubject<TResult> mAfterExecute = PublishSubject.create(); // TODO Lazy
    private Observable<TResult> mResult;
    private Scheduler mScheduler = AndroidScheduler.threadForUI();

    public static <TParam, TResult> AsyncCommand<TParam, TResult> create(Func1<TParam, Observable<TResult>> pAsyncCommand) {
        return new AsyncCommand<TParam, TResult>(pAsyncCommand, null);
    }

    // public static <TParam, TResult> AsyncCommand<TParam, TResult> create(Func1<TParam, Observable<TResult>> pAsyncCommand,
    // Func1<TResult, TResult> pAfterCommand)
    // {
    // return new AsyncCommand<TParam, TResult>(pAsyncCommand, pAfterCommand);
    // }

    protected AsyncCommand(Func1<TParam, Observable<TResult>> pAsyncCommand, Func1<TResult, TResult> pAfterCommand) {
        super(null);
        mCommand = PublishSubject.create();
        Observable<Observable<TResult>> execution = mCommand// .map(notifyExec(mBeforeExecute)) // TODO Ensure on UI Thread
        .map(pAsyncCommand);
        /*
         * .map(notifyExec2(mAfterExecute)) .finallyDo(exec(mAfterExecute))
         */; // TODO Publish?
        mResult = connect(Observable.merge(execution).observeOn(mScheduler).publish()); // .map(pAfterCommand);
    }

    public Observable<TResult> connect(ConnectableObservable<TResult> pObservable) {
        pObservable.connect();
        return pObservable;
    }

    public Func1<TParam, TParam> notifyExec(final PublishSubject<TParam> pPublisher) {
        return new Func1<TParam, TParam>() {
            public TParam call(TParam pParam) {
                pPublisher.onNext(pParam);
                return pParam;
            }
        };
    }

    public Func1<TResult, TResult> notifyExec2(final PublishSubject<TResult> pPublisher) {
        return new Func1<TResult, TResult>() {
            public TResult call(TResult pResult) {
                pPublisher.onNext(pResult);
                return pResult;
            }
        };
    }

    public Action0 exec(final PublishSubject<TParam> pPublisher) {
        return new Action0() {
            public void call() {
                pPublisher.onNext(null);
            }
        };
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
