/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.codexperiments.newsroot.ui.activity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import rx.Scheduler;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Func2;
import android.os.Handler;
import android.os.Looper;

/**
 * Executes work on the Swing UI thread. This scheduler should only be used with actions that execute quickly.
 */
public final class AndroidScheduler extends Scheduler {
    private static final AndroidScheduler sInstance = new AndroidScheduler();
    private static final ExecutorService sIOPool;
    private static final ExecutorService sDatabasePool;

    private Handler uiHandler;

    static {
        sIOPool = createThreadPoolForIO();
        sDatabasePool = createThreadPoolForDatabase();
    }

    private static ExecutorService createThreadPoolForIO() {
        return Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicLong counter = new AtomicLong();

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "RxIOThreadPool-" + counter.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        });
    }

    private static ExecutorService createThreadPoolForDatabase() {
        return Executors.newSingleThreadExecutor();
    }

    public static AndroidScheduler getInstance() {
        return sInstance;
    }

    public static Scheduler threadPoolForIO() {
        return Schedulers.executor(sIOPool);
    }

    public static Scheduler threadPoolForDatabase() {
        return Schedulers.executor(sDatabasePool);
    }

    private AndroidScheduler() {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public <T> Subscription schedule(final T state, final Func2<Scheduler, T, Subscription> action) {
        final AtomicReference<Subscription> sub = new AtomicReference<Subscription>();
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                sub.set(action.call(AndroidScheduler.this, state));
            }
        });
        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                Subscription subscription = sub.get();
                if (subscription != null) {
                    subscription.unsubscribe();
                }
            }
        });
    }

    @Override
    public <T> Subscription schedule(final T state, final Func2<Scheduler, T, Subscription> action, long dueTime, TimeUnit unit) {
        final AtomicReference<Subscription> sub = new AtomicReference<Subscription>();
        long delay = unit.toMillis(dueTime);
        assertThatTheDelayIsValidForTheSwingTimer(delay);

        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                sub.set(action.call(AndroidScheduler.this, state));
            }
        }, dueTime);

        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                timer.cancel();
                timer.purge();

                Subscription subscription = sub.get();
                if (subscription != null) {
                    subscription.unsubscribe();
                }
            }
        });
    }

    @Override
    public <T> Subscription schedulePeriodically(final T state,
                                                 final Func2<Scheduler, T, Subscription> action,
                                                 long initialDelay,
                                                 long c,
                                                 TimeUnit unit)
    {
        final AtomicReference<Subscription> sub = new AtomicReference<Subscription>();
        long delay = unit.toMillis(initialDelay);
        assertThatTheDelayIsValidForTheSwingTimer(delay);

        final Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                action.call(AndroidScheduler.this, state);
            }
        }, initialDelay, initialDelay);

        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                timer.cancel();
                timer.purge();

                Subscription subscription = sub.get();
                if (subscription != null) {
                    subscription.unsubscribe();
                }
            }
        });
    }

    private static void assertThatTheDelayIsValidForTheSwingTimer(long delay) {
        if (delay < 0 || delay > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(String.format("The swing timer only accepts non-negative delays up to %d milliseconds.",
                                                             Integer.MAX_VALUE));
        }
    }
}
