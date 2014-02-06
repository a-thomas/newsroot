package com.codexperiments.newsroot.test.helper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.concurrency.TestScheduler;
import rx.util.functions.Action0;
import android.util.Log;

import com.codexperiments.rx.AndroidScheduler;

public class RxTest {
    public static <T> void subscribeAndWait(final Observable<T> pObservable, final Observer<T> pObserver)
        throws InterruptedException
    {
        final CountDownLatch lCompleted = new CountDownLatch(1);
        pObservable.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<T>() {
            public void onNext(T pValue) {
                pObserver.onNext(pValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
                lCompleted.countDown();
            }

            public void onError(Throwable pThrowable) {
                Log.e(getClass().getSimpleName(), "Error in Observable;", pThrowable);
                pObserver.onError(pThrowable);
                lCompleted.countDown();
            }
        });
        lCompleted.await();
    }

    public static <T> void subscribeAndNotWait(final Observable<T> pObservable, final Observer<T> pObserver)
        throws InterruptedException
    {
        pObservable.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<T>() {
            public void onNext(T pValue) {
                pObserver.onNext(pValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
            }

            public void onError(Throwable pThrowable) {
                Log.e(getClass().getSimpleName(), "Error in Observable;", pThrowable);
                pObserver.onError(pThrowable);
            }
        });
    }

    public static <T> void scheduleOnNext(TestScheduler pScheduler, final Observer<T> observer, final T value, int delay) {
        pScheduler.schedule(new Action0() {
            @Override
            public void call() {
                observer.onNext(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public static <TThrowable extends Throwable> void scheduleOnError(TestScheduler pScheduler,
                                                                      final Observer<?> observer,
                                                                      final TThrowable value,
                                                                      int delay)
    {
        pScheduler.schedule(new Action0() {
            @Override
            public void call() {
                observer.onError(value);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public static void scheduleOnComplete(TestScheduler pScheduler, final Observer<?> observer, int delay) {
        pScheduler.schedule(new Action0() {
            @Override
            public void call() {
                observer.onCompleted();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
