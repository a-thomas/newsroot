package com.codexperiments.newsroot.test.helper;

import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Observer;
import android.util.Log;

import com.codexperiments.newsroot.ui.activity.AndroidScheduler;

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
}