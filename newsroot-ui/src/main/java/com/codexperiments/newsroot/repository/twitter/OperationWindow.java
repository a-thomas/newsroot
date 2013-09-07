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
package com.codexperiments.newsroot.repository.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.concurrency.Schedulers;
import rx.operators.SafeObservableSubscription;
import rx.util.BufferClosing;
import rx.util.BufferOpening;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Func0;
import rx.util.functions.Func1;

public final class OperationWindow {

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window until the {@link Observable} constructed using the
     * {@link Func0} argument, produces a {@link BufferClosing} value. The window is then emitted, and a new window is created to
     * replace it. A new {@link Observable} will be constructed using the provided {@link Func0} object, which will determine when
     * this new window is emitted. When the source {@link Observable} completes or produces an error, the current window is
     * emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is exactly one window
     * actively storing values.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param windowClosingSelector A {@link Func0} object which produces {@link Observable}s. These {@link Observable}s determine
     *            when a window is emitted and replaced by simply producing an {@link BufferClosing} object.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(final Observable<T> source,
                                                                          final Func0<Observable<BufferClosing>> windowClosingSelector)
    {
        return new Func1<Observer<Observable<T>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Observable<T>> observer) {
                NonOverlappingWindows<T> windows = new NonOverlappingWindows<T>(observer);
                WindowCreator<T> creator = new ObservableBasedSingleWindowCreator<T>(windows, windowClosingSelector);
                return source.subscribe(new WindowObserver<T>(windows, observer, creator));
            }
        };
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in the currently active windows. Initially there are no windows active.
     * </p>
     * 
     * <p>
     * Windows can be created by pushing a {@link BufferOpening} value to the "BufferOpenings" {@link Observable}. This creates a
     * new window which will then start recording values which are produced by the "source" {@link Observable}. Additionally the
     * "windowClosingSelector" will be used to construct an {@link Observable} which can produce {@link BufferClosing} values.
     * When it does so it will close this (and only this) newly created window. When the source {@link Observable} completes or
     * produces an error, all windows are emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that when using this operation <strong>multiple overlapping windows</strong> could be active at any one point.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param BufferOpenings An {@link Observable} which when it produces a {@link BufferOpening} value will create a new window
     *            which instantly starts recording the "source" {@link Observable}.
     * @param windowClosingSelector A {@link Func0} object which produces {@link Observable}s. These {@link Observable}s determine
     *            when a window is emitted and replaced by simply producing an {@link BufferClosing} object.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(final Observable<T> source,
                                                                          final Observable<BufferOpening> BufferOpenings,
                                                                          final Func1<BufferOpening, Observable<BufferClosing>> windowClosingSelector)
    {
        return new Func1<Observer<Observable<T>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Observable<T>> observer) {
                OverlappingWindows<T> windows = new OverlappingWindows<T>(observer);
                WindowCreator<T> creator = new ObservableBasedMultiWindowCreator<T>(windows,
                                                                                    BufferOpenings,
                                                                                    windowClosingSelector);
                return source.subscribe(new WindowObserver<T>(windows, observer, creator));
            }
        };
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window until the window contains a specified number of elements.
     * The window is then emitted, and a new window is created to replace it. When the source {@link Observable} completes or
     * produces an error, the current window is emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is exactly one window
     * actively storing values.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param count The number of elements a window should have before being emitted and replaced.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(Observable<T> source, int count) {
        return window(source, count, count);
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in all active windows until the window contains a specified number of
     * elements. The window is then emitted. Windows are created after a certain amount of values have been received. When the
     * source {@link Observable} completes or produces an error, the currently active windows are emitted, and the event is
     * propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation can produce <strong>non-connected, connected non-overlapping, or overlapping windows</strong>
     * depending on the input parameters.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param count The number of elements a window should have before being emitted.
     * @param skip The interval with which windows have to be created. Note that when "skip" == "count" that this is the same as
     *            calling {@link OperationWindow#window(Observable, int)}. If "skip" < "count", this window operation will produce
     *            overlapping windows and if "skip" > "count" non-overlapping windows will be created and some values will not be
     *            pushed into a window at all!
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(final Observable<T> source,
                                                                          final int count,
                                                                          final int skip)
    {
        return new Func1<Observer<Observable<T>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Observable<T>> observer) {
                Windows<T> windows = new SizeBasedWindows<T>(observer, count);
                WindowCreator<T> creator = new SkippingWindowCreator<T>(windows, skip);
                return source.subscribe(new WindowObserver<T>(windows, observer, creator));
            }
        };
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window. Periodically the window is emitted and replaced with a new
     * window. How often this is done depends on the specified timespan. When the source {@link Observable} completes or produces
     * an error, the current window is emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is exactly one window
     * actively storing values.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param timespan The amount of time all windows must be actively collect values before being emitted.
     * @param unit The {@link TimeUnit} defining the unit of time for the timespan.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(Observable<T> source, long timespan, TimeUnit unit) {
        return window(source, timespan, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window. Periodically the window is emitted and replaced with a new
     * window. How often this is done depends on the specified timespan. When the source {@link Observable} completes or produces
     * an error, the current window is emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is exactly one window
     * actively storing values.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param timespan The amount of time all windows must be actively collect values before being emitted.
     * @param unit The {@link TimeUnit} defining the unit of time for the timespan.
     * @param scheduler The {@link Scheduler} to use for timing windows.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(final Observable<T> source,
                                                                          final long timespan,
                                                                          final TimeUnit unit,
                                                                          final Scheduler scheduler)
    {
        return new Func1<Observer<Observable<T>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Observable<T>> observer) {
                NonOverlappingWindows<T> windows = new NonOverlappingWindows<T>(observer);
                WindowCreator<T> creator = new TimeBasedWindowCreator<T>(windows, timespan, unit, scheduler);
                return source.subscribe(new WindowObserver<T>(windows, observer, creator));
            }
        };
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window. Periodically the window is emitted and replaced with a new
     * window. How often this is done depends on the specified timespan. Additionally the window is automatically emitted once it
     * reaches a specified number of elements. When the source {@link Observable} completes or produces an error, the current
     * window is emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is exactly one window
     * actively storing values.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param timespan The amount of time all windows must be actively collect values before being emitted.
     * @param unit The {@link TimeUnit} defining the unit of time for the timespan.
     * @param count The maximum size of the window. Once a window reaches this size, it is emitted.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(Observable<T> source,
                                                                          long timespan,
                                                                          TimeUnit unit,
                                                                          int count)
    {
        return window(source, timespan, unit, count, Schedulers.threadPoolForComputation());
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window. Periodically the window is emitted and replaced with a new
     * window. How often this is done depends on the specified timespan. Additionally the window is automatically emitted once it
     * reaches a specified number of elements. When the source {@link Observable} completes or produces an error, the current
     * window is emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation only produces <strong>non-overlapping windows</strong>. At all times there is exactly one window
     * actively storing values.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param timespan The amount of time all windows must be actively collect values before being emitted.
     * @param unit The {@link TimeUnit} defining the unit of time for the timespan.
     * @param count The maximum size of the window. Once a window reaches this size, it is emitted.
     * @param scheduler The {@link Scheduler} to use for timing windows.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(final Observable<T> source,
                                                                          final long timespan,
                                                                          final TimeUnit unit,
                                                                          final int count,
                                                                          final Scheduler scheduler)
    {
        return new Func1<Observer<Observable<T>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Observable<T>> observer) {
                Windows<T> windows = new TimeAndSizeBasedWindows<T>(observer, count, timespan, unit, scheduler);
                WindowCreator<T> creator = new SingleWindowCreator<T>(windows);
                return source.subscribe(new WindowObserver<T>(windows, observer, creator));
            }
        };
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window. Periodically the window is emitted and replaced with a new
     * window. How often this is done depends on the specified timespan. The creation of windows is also periodical. How often
     * this is done depends on the specified timeshift. When the source {@link Observable} completes or produces an error, the
     * current window is emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation can produce <strong>non-connected, or overlapping windows</strong> depending on the input
     * parameters.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param timespan The amount of time all windows must be actively collect values before being emitted.
     * @param timeshift The amount of time between creating windows.
     * @param unit The {@link TimeUnit} defining the unit of time for the timespan.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(Observable<T> source,
                                                                          long timespan,
                                                                          long timeshift,
                                                                          TimeUnit unit)
    {
        return window(source, timespan, timeshift, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * <p>
     * This method creates a {@link Func1} object which represents the window operation. This operation takes values from the
     * specified {@link Observable} source and stores them in a window. Periodically the window is emitted and replaced with a new
     * window. How often this is done depends on the specified timespan. The creation of windows is also periodical. How often
     * this is done depends on the specified timeshift. When the source {@link Observable} completes or produces an error, the
     * current window is emitted, and the event is propagated to all subscribed {@link Observer}s.
     * </p>
     * 
     * <p>
     * Note that this operation can produce <strong>non-connected, or overlapping windows</strong> depending on the input
     * parameters.
     * </p>
     * 
     * @param source The {@link Observable} which produces values.
     * @param timespan The amount of time all windows must be actively collect values before being emitted.
     * @param timeshift The amount of time between creating windows.
     * @param unit The {@link TimeUnit} defining the unit of time for the timespan.
     * @param scheduler The {@link Scheduler} to use for timing windows.
     * @return the {@link Func1} object representing the specified window operation.
     */
    public static <T> Func1<Observer<Observable<T>>, Subscription> window(final Observable<T> source,
                                                                          final long timespan,
                                                                          final long timeshift,
                                                                          final TimeUnit unit,
                                                                          final Scheduler scheduler)
    {
        return new Func1<Observer<Observable<T>>, Subscription>() {
            @Override
            public Subscription call(final Observer<Observable<T>> observer) {
                OverlappingWindows<T> windows = new TimeBasedWindows<T>(observer, timespan, unit, scheduler);
                WindowCreator<T> creator = new TimeBasedWindowCreator<T>(windows, timeshift, unit, scheduler);
                return source.subscribe(new WindowObserver<T>(windows, observer, creator));
            }
        };
    }

    /**
     * This {@link WindowObserver} object can be constructed using a {@link Windows} object, a {@link Observer} object, and a
     * {@link WindowCreator} object. The {@link WindowCreator} will manage the creation, and in some rare cases emission of
     * internal {@link Window} objects in the specified {@link Windows} object. Under normal circumstances the {@link Windows}
     * object specifies when a created {@link Window} is emitted.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class WindowObserver<T> implements Observer<T> {

        private final Windows<T> windows;
        private final Observer<Observable<T>> observer;
        private final WindowCreator<T> creator;

        public WindowObserver(Windows<T> windows, Observer<Observable<T>> observer, WindowCreator<T> creator) {
            this.observer = observer;
            this.creator = creator;
            this.windows = windows;
        }

        @Override
        public void onCompleted() {
            creator.stop();
            windows.emitAllWindows();
            observer.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            creator.stop();
            windows.emitAllWindows();
            observer.onError(e);
        }

        @Override
        public void onNext(T args) {
            creator.onValuePushed();
            windows.pushValue(args);
        }
    }

    /**
     * This interface defines a way which specifies when to create a new internal {@link Window} object.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private interface WindowCreator<T> {
        /**
         * Signifies a onNext event.
         */
        void onValuePushed();

        /**
         * Signifies a onCompleted or onError event. Should be used to clean up open subscriptions and other still running
         * background tasks.
         */
        void stop();
    }

    /**
     * This {@link WindowCreator} creates a new {@link Window} when it is initialized, but provides no additional functionality.
     * This class should primarily be used when the internal {@link Window} is closed externally.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class SingleWindowCreator<T> implements WindowCreator<T> {

        public SingleWindowCreator(Windows<T> windows) {
            windows.createWindow();
        }

        @Override
        public void onValuePushed() {
            // Do nothing.
        }

        @Override
        public void stop() {
            // Do nothing.
        }
    }

    /**
     * This {@link WindowCreator} creates a new {@link Window} whenever it receives an object from the provided {@link Observable}
     * created with the windowClosingSelector {@link Func0}.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class ObservableBasedSingleWindowCreator<T> implements WindowCreator<T> {

        private final SafeObservableSubscription subscription = new SafeObservableSubscription();
        private final Func0<Observable<BufferClosing>> windowClosingSelector;
        private final NonOverlappingWindows<T> windows;

        public ObservableBasedSingleWindowCreator(NonOverlappingWindows<T> windows,
                                                  Func0<Observable<BufferClosing>> windowClosingSelector)
        {
            this.windows = windows;
            this.windowClosingSelector = windowClosingSelector;

            windows.createWindow();
            listenForWindowEnd();
        }

        private void listenForWindowEnd() {
            Observable<BufferClosing> closingObservable = windowClosingSelector.call();
            closingObservable.subscribe(new Action1<BufferClosing>() {
                @Override
                public void call(BufferClosing closing) {
                    windows.emitAndReplaceWindow();
                    listenForWindowEnd();
                }
            });
        }

        @Override
        public void onValuePushed() {
            // Ignore value pushes.
        }

        @Override
        public void stop() {
            subscription.unsubscribe();
        }
    }

    /**
     * This {@link WindowCreator} creates a new {@link Window} whenever it receives an object from the provided BufferOpenings
     * {@link Observable}, and closes the corresponding {@link Window} object when it receives an object from the provided
     * {@link Observable} created with the windowClosingSelector {@link Func1}.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class ObservableBasedMultiWindowCreator<T> implements WindowCreator<T> {

        private final SafeObservableSubscription subscription = new SafeObservableSubscription();

        public ObservableBasedMultiWindowCreator(final OverlappingWindows<T> windows,
                                                 Observable<BufferOpening> BufferOpenings,
                                                 final Func1<BufferOpening, Observable<BufferClosing>> windowClosingSelector)
        {
            subscription.wrap(BufferOpenings.subscribe(new Action1<BufferOpening>() {
                @Override
                public void call(BufferOpening opening) {
                    final Window<T> window = windows.createWindow();
                    Observable<BufferClosing> closingObservable = windowClosingSelector.call(opening);

                    closingObservable.subscribe(new Action1<BufferClosing>() {
                        @Override
                        public void call(BufferClosing closing) {
                            windows.emitWindow(window);
                        }
                    });
                }
            }));
        }

        @Override
        public void onValuePushed() {
            // Ignore value pushes.
        }

        @Override
        public void stop() {
            subscription.unsubscribe();
        }
    }

    /**
     * This {@link WindowCreator} creates a new {@link Window} every time after a fixed period of time has elapsed.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class TimeBasedWindowCreator<T> implements WindowCreator<T> {

        private final SafeObservableSubscription subscription = new SafeObservableSubscription();

        public TimeBasedWindowCreator(final NonOverlappingWindows<T> windows, long time, TimeUnit unit, Scheduler scheduler) {
            this.subscription.wrap(scheduler.schedulePeriodically(new Action0() {
                @Override
                public void call() {
                    windows.emitAndReplaceWindow();
                }
            }, 0, time, unit));
        }

        public TimeBasedWindowCreator(final OverlappingWindows<T> windows, long time, TimeUnit unit, Scheduler scheduler) {
            this.subscription.wrap(scheduler.schedulePeriodically(new Action0() {
                @Override
                public void call() {
                    windows.createWindow();
                }
            }, 0, time, unit));
        }

        @Override
        public void onValuePushed() {
            // Do nothing: windows are created periodically.
        }

        @Override
        public void stop() {
            subscription.unsubscribe();
        }
    }

    /**
     * This {@link WindowCreator} creates a new {@link Window} every time after it has seen a certain amount of elements.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class SkippingWindowCreator<T> implements WindowCreator<T> {

        private final AtomicInteger skipped = new AtomicInteger(1);
        private final Windows<T> windows;
        private final int skip;

        public SkippingWindowCreator(Windows<T> windows, int skip) {
            this.windows = windows;
            this.skip = skip;
        }

        @Override
        public void onValuePushed() {
            if (skipped.decrementAndGet() == 0) {
                skipped.set(skip);
                windows.createWindow();
            }
        }

        @Override
        public void stop() {
            // Nothing to stop: we're not using a Scheduler.
        }
    }

    /**
     * This class is an extension on the {@link Windows} class which only supports one active (not yet emitted) internal
     * {@link Window} object.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class NonOverlappingWindows<T> extends Windows<T> {

        private final Object lock = new Object();

        public NonOverlappingWindows(Observer<Observable<T>> observer) {
            super(observer);
        }

        public Window<T> emitAndReplaceWindow() {
            synchronized (lock) {
                emitWindow(getWindow());
                return createWindow();
            }
        }

        @Override
        public void pushValue(T value) {
            synchronized (lock) {
                super.pushValue(value);
            }
        }
    }

    /**
     * This class is an extension on the {@link Windows} class which actually has no additional behavior than its super class.
     * Classes extending this class, are expected to support two or more active (not yet emitted) internal {@link Window} objects.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class OverlappingWindows<T> extends Windows<T> {
        public OverlappingWindows(Observer<Observable<T>> observer) {
            super(observer);
        }
    }

    /**
     * This class is an extension on the {@link Windows} class. Every internal window has a has a maximum time to live and a
     * maximum internal capacity. When the window has reached the end of its life, or reached its maximum internal capacity it is
     * automatically emitted.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class TimeAndSizeBasedWindows<T> extends Windows<T> {

        private final ConcurrentMap<Window<T>, Subscription> subscriptions = new ConcurrentHashMap<Window<T>, Subscription>();

        private final Scheduler scheduler;
        private final long maxTime;
        private final TimeUnit unit;
        private final int maxSize;

        public TimeAndSizeBasedWindows(Observer<Observable<T>> observer,
                                       int maxSize,
                                       long maxTime,
                                       TimeUnit unit,
                                       Scheduler scheduler)
        {
            super(observer);
            this.maxSize = maxSize;
            this.maxTime = maxTime;
            this.unit = unit;
            this.scheduler = scheduler;
        }

        @Override
        public Window<T> createWindow() {
            final Window<T> window = super.createWindow();
            subscriptions.put(window, scheduler.schedule(new Action0() {
                @Override
                public void call() {
                    emitWindow(window);
                }
            }, maxTime, unit));
            return window;
        }

        @Override
        public void emitWindow(Window<T> window) {
            Subscription subscription = subscriptions.remove(window);
            if (subscription == null) {
                // Window was already emitted.
                return;
            }

            subscription.unsubscribe();
            super.emitWindow(window);
            createWindow();
        }

        @Override
        public void pushValue(T value) {
            super.pushValue(value);

            Window<T> window;
            while ((window = getWindow()) != null) {
                if (window.contents.size() >= maxSize) {
                    emitWindow(window);
                } else {
                    // Window is not at full capacity yet, and neither will remaining windows be so we can terminate.
                    break;
                }
            }
        }
    }

    /**
     * This class is an extension on the {@link Windows} class. Every internal window has a has a maximum time to live. When the
     * window has reached the end of its life it is automatically emitted.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class TimeBasedWindows<T> extends OverlappingWindows<T> {

        private final ConcurrentMap<Window<T>, Subscription> subscriptions = new ConcurrentHashMap<Window<T>, Subscription>();

        private final Scheduler scheduler;
        private final long time;
        private final TimeUnit unit;

        public TimeBasedWindows(Observer<Observable<T>> observer, long time, TimeUnit unit, Scheduler scheduler) {
            super(observer);
            this.time = time;
            this.unit = unit;
            this.scheduler = scheduler;
        }

        @Override
        public Window<T> createWindow() {
            final Window<T> window = super.createWindow();
            subscriptions.put(window, scheduler.schedule(new Action0() {
                @Override
                public void call() {
                    emitWindow(window);
                }
            }, time, unit));
            return window;
        }

        @Override
        public void emitWindow(Window<T> window) {
            subscriptions.remove(window);
            super.emitWindow(window);
        }
    }

    /**
     * This class is an extension on the {@link Windows} class. Every internal window has a fixed maximum capacity. When the
     * window has reached its maximum capacity it is automatically emitted.
     * 
     * @param <T> The type of object all internal {@link Window} objects record.
     */
    private static class SizeBasedWindows<T> extends Windows<T> {

        private final int size;

        public SizeBasedWindows(Observer<Observable<T>> observer, int size) {
            super(observer);
            this.size = size;
        }

        @Override
        public void pushValue(T value) {
            super.pushValue(value);

            Window<T> window;
            while ((window = getWindow()) != null) {
                if (window.contents.size() >= size) {
                    emitWindow(window);
                } else {
                    // Window is not at full capacity yet, and neither will remaining windows be so we can terminate.
                    break;
                }
            }
        }
    }

    /**
     * This class represents an object which contains and manages multiple {@link Window} objects.
     * 
     * @param <T> The type of objects which the internal {@link Window} objects record.
     */
    private static class Windows<T> {

        private final Queue<Window<T>> windows = new ConcurrentLinkedQueue<Window<T>>();
        private final Observer<Observable<T>> observer;

        /**
         * Constructs a new {@link Windows} object for the specified {@link Observer}.
         * 
         * @param observer The {@link Observer} to which this object will emit its internal {@link Window} objects to when
         *            requested.
         */
        public Windows(Observer<Observable<T>> observer) {
            this.observer = observer;
        }

        /**
         * This method will instantiate a new {@link Window} object and register it internally.
         * 
         * @return The constructed empty {@link Window} object.
         */
        public Window<T> createWindow() {
            Window<T> window = new Window<T>();
            windows.add(window);
            return window;
        }

        /**
         * This method emits all not yet emitted {@link Window} objects.
         */
        public void emitAllWindows() {
            Window<T> window;
            while ((window = windows.poll()) != null) {
                observer.onNext(Observable.from(window.getContents()));
            }
        }

        /**
         * This method emits the specified {@link Window} object.
         * 
         * @param window The {@link Window} to emit.
         */
        public void emitWindow(Window<T> window) {
            if (!windows.remove(window)) {
                // Concurrency issue: Window is already emitted!
                return;
            }
            observer.onNext(Observable.from(window.getContents()));
        }

        /**
         * @return The oldest (in case there are multiple) {@link Window} object.
         */
        public Window<T> getWindow() {
            return windows.peek();
        }

        /**
         * This method pushes a value to all not yet emitted {@link Window} objects.
         * 
         * @param value The value to push to all not yet emitted {@link Window} objects.
         */
        public void pushValue(T value) {
            List<Window<T>> copy = new ArrayList<Window<T>>(windows);
            for (Window<T> window : copy) {
                window.pushValue(value);
            }
        }
    }

    /**
     * This class represents a single window: A sequence of recorded values.
     * 
     * @param <T> The type of objects which this {@link Window} can hold.
     */
    private static class Window<T> {
        private final List<T> contents = new ArrayList<T>();

        /**
         * Appends a specified value to the {@link Window}.
         * 
         * @param value The value to append to the {@link Window}.
         */
        public void pushValue(T value) {
            contents.add(value);
        }

        /**
         * @return The mutable underlying {@link List} which contains all the recorded values in this {@link Window} object.
         */
        public List<T> getContents() {
            return contents;
        }
    }
}
