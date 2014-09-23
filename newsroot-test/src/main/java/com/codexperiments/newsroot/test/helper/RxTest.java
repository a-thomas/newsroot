package com.codexperiments.newsroot.test.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.schedulers.TestScheduler;

import com.codexperiments.rx.AndroidScheduler;
import com.google.common.collect.Lists;

public class RxTest {
    /**
     * Subscribe to the given observable and return immediately a latch that can be used to wait for observer completion.
     * 
     * @param pObservable Observable to subscribe to.
     * @param pObserver Observer to wait for completion.
     * @return A latch to wait for observer completion.
     * @throws InterruptedException
     */
    public static <TValue> CountDownLatch subscribeAndReturn(final Observable<TValue> pObservable,
                                                             final Observer<TValue> pObserver) throws InterruptedException
    {
        final CountDownLatch lCompleted = new CountDownLatch(1);
        pObservable.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<TValue>() {
            public void onNext(TValue pValue) {
                pObserver.onNext(pValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
                lCompleted.countDown();
            }

            public void onError(Throwable pThrowable) {
                pObserver.onError(pThrowable);
                lCompleted.countDown();
            }
        });
        return lCompleted;
    }

    /**
     * Subscribe to the given observable and wait until the given observer has completed.
     * 
     * @param pObservable Observable to subscribe to.
     * @param pObserver Observer to wait for completion.
     * @throws InterruptedException
     */
    public static <TValue> void subscribeAndWait(final Observable<TValue> pObservable, final Observer<TValue> pObserver)
        throws InterruptedException
    {
        final CountDownLatch lCompleted = new CountDownLatch(1);
        pObservable.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<TValue>() {
            public void onNext(TValue pValue) {
                pObserver.onNext(pValue);
            }

            public void onCompleted() {
                pObserver.onCompleted();
                lCompleted.countDown();
            }

            public void onError(Throwable pThrowable) {
                pObserver.onError(pThrowable);
                lCompleted.countDown();
            }
        });
        lCompleted.await();
    }

    /**
     * Subscribes multiple times and wait for completion of all subscription. Observers are just plain mock created and returned
     * by this function. Observer given in parameter is inserted in the middle of the set other subscribers.
     * 
     * @param pTimes Number of subscribers (including
     * @param pObservable Observable to subscribe to.
     * @param pObserver Facultative observer that will be inserted in the set of subscribing observers.
     * @return List of created observers, excluding pObserver.
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    public static <TValue> List<Observer<TValue>> subscribeAndWaitMultipleTimes(final int pTimes,
                                                                                final Observable<TValue> pObservable,
                                                                                final Observer<TValue> pObserver)
        throws InterruptedException
    {
        final CountDownLatch lCompleted = new CountDownLatch(pTimes);
        List<Observer<TValue>> lMockObservers = Lists.newArrayList();

        for (int i = 0; i < pTimes; ++i) {
            final Observer<TValue> lObserver;
            if (i == (pTimes / 2)) {
                lObserver = pObserver;
            } else {
                lObserver = mock(Observer.class, withSettings().name("test_observer_" + i).verboseLogging());
                lMockObservers.add(lObserver);
            }

            pObservable.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<TValue>() {
                public void onNext(TValue pValue) {
                    lObserver.onNext(pValue);
                }

                public void onCompleted() {
                    lObserver.onCompleted();
                    lCompleted.countDown();
                }

                public void onError(Throwable pThrowable) {
                    lObserver.onError(pThrowable);
                    lCompleted.countDown();
                }
            });
        }
        lCompleted.await();
        return lMockObservers;
    }

    /**
     * Shortcut to schedule an onNext() call with a given delay.
     * 
     * @param pScheduler Test scheduler.
     * @param pObserver Observer on which onNext() has to be triggered.
     * @param pValue Value to pass to onNext().
     * @param pDelay Delay to wait before sending command (absolute delay, i.e. not cumulative).
     */
    public static <TValue> void scheduleOnNext(TestScheduler pScheduler,
                                               final Observer<TValue> pObserver,
                                               final TValue pValue,
                                               int pDelay)
    {
        pScheduler.advanceTimeBy(pDelay, TimeUnit.MILLISECONDS);
        pObserver.onNext(pValue);
//        pScheduler.schedule(new Action0() {
//            @Override
//            public void call() {
//                pObserver.onNext(pValue);
//            }
//        }, pDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Shortcut to schedule an onError() call with a given delay.
     * 
     * @param pScheduler Test scheduler.
     * @param pObserver Observer on which onError() has to be triggered.
     * @param pThrowable Value to pass to onError().
     * @param pDelay Delay to wait before sending command (absolute delay, i.e. not cumulative).
     */
    public static <TError extends Throwable> void scheduleOnError(TestScheduler pScheduler,
                                                                  final Observer<?> pObserver,
                                                                  final TError pThrowable,
                                                                  int pDelay)
    {
        pScheduler.advanceTimeBy(pDelay, TimeUnit.MILLISECONDS);
        pObserver.onError(pThrowable);
//        pScheduler.schedule(new Action0() {
//            @Override
//            public void call() {
//                pObserver.onError(pThrowable);
//            }
//        }, pDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Shortcut to schedule an onCompleted() call with a given delay.
     * 
     * @param pScheduler Test scheduler.
     * @param pObserver Observer on which onCompleted() has to be triggered.
     * @param pDelay Delay to wait before sending command (absolute delay, i.e. not cumulative).
     */
    public static void scheduleOnComplete(TestScheduler pScheduler, final Observer<?> pObserver, int pDelay) {
        pScheduler.advanceTimeBy(pDelay, TimeUnit.MILLISECONDS);
        pObserver.onCompleted();
//        pScheduler.schedule(new Action0() {
//            @Override
//            public void call() {
//                pObserver.onCompleted();
//            }
//        }, pDelay, TimeUnit.MILLISECONDS);
    }
}
