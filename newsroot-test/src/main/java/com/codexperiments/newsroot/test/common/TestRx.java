package com.codexperiments.newsroot.test.common;

import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Observer;

import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

public class TestRx {
    public static <T> void subscribeAndWait(final Observable<T> pObservable, final Observer<T> pObserver)
        throws InterruptedException
    {
        final CountDownLatch lCompleted = new CountDownLatch(1);
        pObservable.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<T>() {
            public void onNext(T pPageValue) {
                pObserver.onNext(pPageValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
                lCompleted.countDown();
            }

            public void onError(Throwable pThrowable) {
                pObserver.onError(pThrowable);
            }
        });
        lCompleted.await();
    }
}
