package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.observables.ConnectableObservable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import rx.util.functions.Action0;
import rx.util.functions.Func1;

import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class AsyncCommand<TParam, TResult> /* extends Observable<TResult> implements Observer<TParam> */{
    // ScheduledSubject<Exception> exceptions;
    private CompositeSubscription mSubscriptions;
    private PublishSubject<TParam> mCommand;
    // private PublishSubject<TParam> mBeforeExecute = PublishSubject.create(); // TODO Lazy
    // private PublishSubject<TResult> mAfterExecute = PublishSubject.create(); // TODO Lazy
    private PublishSubject<Boolean> mIsRunning;// TODO Lazy
    private ConnectableObservable<TResult> mResult;
    private Scheduler mScheduler = AndroidScheduler.threadForUI();

    public static <TParam, TResult> AsyncCommand<TParam, TResult> create(CompositeSubscription pSubscriptions) {
        return new AsyncCommand<TParam, TResult>(pSubscriptions);
    }

    // public static <TParam, TResult> AsyncCommand<TParam, TResult> create(Func1<TParam, Observable<TResult>> pAsyncCommand,
    // Func1<TResult, TResult> pAfterCommand)
    // {
    // return new AsyncCommand<TParam, TResult>(pAsyncCommand, pAfterCommand);
    // }

    protected AsyncCommand(CompositeSubscription pSubscriptions) {
        super(null);
        mCommand = PublishSubject.create();
        mIsRunning = PublishSubject.create();

        // Observable<Observable<TResult>> execution = runningIndicator(pAsyncCommand);
        // mCommand// .map(notifyExec(mBeforeExecute)) // TODO Ensure on UI Thread
        // .map(new Func1<TParam, Observable<TResult>>() {
        // public Observable<TResult> call(TParam pT1) {
        // return startRunning(pAsyncCommand.call(pT1)); // TODO Catch
        // }
        // });
        /*
         * .map(notifyExec2(mAfterExecute)) .finallyDo(exec(mAfterExecute))
         */; // TODO Publish?
        mResult = null;
        mSubscriptions = pSubscriptions;
    }

    public ConnectableObservable<TResult> register(final Func1<TParam, Observable<TResult>> pAsyncCommand) {
        if (mResult != null) {
            throw new IllegalArgumentException("Cannot subscribe to a command if no command has been registered.");
        }
        // Observable<Observable<TResult>> execution = runningIndicator(pAsyncCommand);
        // mCommand// .map(notifyExec(mBeforeExecute)) // TODO Ensure on UI Thread
        // .map(new Func1<TParam, Observable<TResult>>() {
        // public Observable<TResult> call(TParam pT1) {
        // return startRunning(pAsyncCommand.call(pT1)); // TODO Catch
        // }
        // });
        /*
         * .map(notifyExec2(mAfterExecute)) .finallyDo(exec(mAfterExecute))
         */; // TODO Publish?
        Observable<Observable<TResult>> asyncCommandWithNotifications = mCommand.map(new Func1<TParam, Observable<TResult>>() {
            public Observable<TResult> call(TParam pAsyncObservable) {
                return pAsyncCommand.call(pAsyncObservable) //
                                    .observeOn(mScheduler)
                                    // Notify when command ends.
                                    .finallyDo(new Action0() {
                                        public void call() {
                                            mIsRunning.onNext(Boolean.FALSE);
                                        }
                                    }); // TODO Catch & toRef
            }
        }).map(new Func1<Observable<TResult>, Observable<TResult>>() {
            public Observable<TResult> call(Observable<TResult> pAsyncObservable) {
                // Notify when command starts
                mIsRunning.onNext(Boolean.TRUE);
                return pAsyncObservable;
            }
        });

        mResult = Observable.merge(asyncCommandWithNotifications).observeOn(mScheduler).publish(); // .map(pAfterCommand);
        mResult.connect();
        return mResult;
    }

    @Override
    public Subscription subscribe(Observer<? super TResult> pObserver) {
        if (mResult == null) {
            throw new IllegalArgumentException("Cannot subscribe to a command if no command has been registered.");
        }
        Subscription lSubscription = mResult.subscribe(pObserver);
        mSubscriptions.add(lSubscription);
        return lSubscription;
    }

    public Observable<Boolean> isRunning() {
        return mIsRunning;
    }

    public void execute() {
        mCommand.onNext(null);
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
