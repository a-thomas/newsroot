package com.codexperiments.rx;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.operators.SafeObservableSubscription;
import rx.subjects.Subject;
import rx.util.functions.Func0;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

public class OperationFeedback {
    private static <TInput, TOutput> Subject<TOutput, TInput> createFeedbackSubject(final Func1<TOutput, TInput> pFeedbackValue,
                                                                                    final int pMaxRecursion)
    {
        final TInput initialValue = pFeedbackValue.call(null);
        final ConcurrentHashMap<Subscription, Observer<? super TInput>> observers = new ConcurrentHashMap<Subscription, Observer<? super TInput>>();
        final AtomicReference<TInput> currentValue = new AtomicReference<TInput>(initialValue);

        OnSubscribeFunc<TInput> lOnSubscribe = new OnSubscribeFunc<TInput>() {
            public Subscription onSubscribe(final Observer<? super TInput> pObserver) {
                final SafeObservableSubscription subscription = new SafeObservableSubscription();

                subscription.wrap(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        // on unsubscribe remove it from the map of outbound observers to notify
                        observers.remove(subscription);
                        if (observers.size() == 0) {
                            subscription.unsubscribe();
                        }
                    }
                });

                // on subscribe add it to the map of outbound observers to notify
                observers.put(subscription, pObserver);

                pObserver.onNext(currentValue.get());
                return subscription;
            }
        };

        return new Subject<TOutput, TInput>(lOnSubscribe) {
            int mPageCount = pMaxRecursion - 1;

            public void onNext(TOutput pTValue) {
                TInput lQuery = pFeedbackValue.call(pTValue);

                if ((lQuery != null) && (mPageCount > 0)) {
                    currentValue.set(lQuery);
                    --mPageCount;
                    for (Observer<? super TInput> observer : observers.values()) {
                        observer.onNext(lQuery);
                    }
                } else {
                    // lOnSubscribe.subscription.unsubscribe();
                    onCompleted(); // TODO
                }
            }

            public void onCompleted() {
                // TODO Check if already completed.
                for (Observer<? super TInput> observer : observers.values()) {
                    observer.onCompleted();
                }
            }

            public void onError(Throwable pThrowable) {
                for (Observer<? super TInput> observer : observers.values()) {
                    observer.onError(pThrowable);
                }
            }
        };
    }

    public static <TInput, TOutput> Observable<TOutput> feedback(final int pMaxRecursion,
                                                                 final Func1<TOutput, TInput> pFeedbackValue,
                                                                 final Func1<Observable<TInput>, Observable<TOutput>> pFunc1)
    {
        return Observable.defer(new Func0<Observable<? extends TOutput>>() {
            public Observable<? extends TOutput> call() {
                return Observable.create(new OnSubscribeFunc<TOutput>() {
                    final Subject<TOutput, TInput> feed1 = createFeedbackSubject(pFeedbackValue, pMaxRecursion);
                    final Observable<TOutput> pOutputObservable = pFunc1.call(feed1);

                    public Subscription onSubscribe(final Observer<? super TOutput> pOutputObserver) {
                        final Subscription lInnerSubscription = pOutputObservable.subscribe(new Observer<TOutput>() {
                            public void onNext(TOutput pTValue) {
                                pOutputObserver.onNext(pTValue);
                                feed1.onNext(pTValue);
                            }

                            public void onCompleted() {
                                pOutputObserver.onCompleted();
                                feed1.onCompleted();
                            }

                            public void onError(Throwable pThrowable) {
                                pOutputObserver.onError(pThrowable);
                                feed1.onError(pThrowable);
                            }
                        });
                        return new Subscription() {
                            public void unsubscribe() {
                                lInnerSubscription.unsubscribe();
                            }
                        };
                    }
                });
            }
        });
    }

    private static <TOutput> Subject<TOutput, TOutput> createFeedbackSubject2(// final Func1<TOutput, TInput> pFeedbackValue,
    final TOutput pInitialValue,
                                                                              final int pMaxRecursion)
    {
        final TOutput initialValue = pInitialValue;
        final ConcurrentHashMap<Subscription, Observer<? super TOutput>> observers = new ConcurrentHashMap<Subscription, Observer<? super TOutput>>();
        final AtomicReference<TOutput> currentValue = new AtomicReference<TOutput>(initialValue);

        OnSubscribeFunc<TOutput> lOnSubscribe = new OnSubscribeFunc<TOutput>() {
            public Subscription onSubscribe(final Observer<? super TOutput> pObserver) {
                final SafeObservableSubscription subscription = new SafeObservableSubscription();

                subscription.wrap(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        // on unsubscribe remove it from the map of outbound observers to notify
                        observers.remove(subscription);
                        if (observers.size() == 0) {
                            subscription.unsubscribe();
                        }
                    }
                });

                // on subscribe add it to the map of outbound observers to notify
                observers.put(subscription, pObserver);

                pObserver.onNext(currentValue.get());
                return subscription;
            }
        };

        return new Subject<TOutput, TOutput>(lOnSubscribe) {
            int mPageCount = pMaxRecursion - 1;

            public void onNext(TOutput pTValue) {
                // TOutput lQuery = pFeedbackValue.call(pTValue);

                if ((pTValue != null) && (mPageCount > 0)) {
                    currentValue.set(pTValue);
                    --mPageCount;
                    for (Observer<? super TOutput> observer : observers.values()) {
                        observer.onNext(pTValue);
                    }
                } else {
                    // lOnSubscribe.subscription.unsubscribe();
                    onCompleted(); // TODO
                }
            }

            public void onCompleted() {
                // TODO Check if already completed.
                for (Observer<? super TOutput> observer : observers.values()) {
                    observer.onCompleted();
                }
            }

            public void onError(Throwable pThrowable) {
                for (Observer<? super TOutput> observer : observers.values()) {
                    observer.onError(pThrowable);
                }
            }
        };
    }

    private static final Object EOR = new Object();

    @SuppressWarnings("unchecked")
    public static <TInput> TInput endOFRecursion() {
        return (TInput) EOR;
    }

    public static <TInput, TOutput> Observable<TOutput> feedback2(final int pMaxRecursion,
                                                                  final Observable<TInput> src,
                                                                  final Func2<Observable<TInput>, Observable<TOutput>, Observable<TInput>> pFeedbackValue,
                                                                  final Func1<Observable<TInput>, Observable<TOutput>> pFunc1)
    {
        return Observable.defer(new Func0<Observable<? extends TOutput>>() {
            public Observable<? extends TOutput> call() {
                return Observable.create(new OnSubscribeFunc<TOutput>() {
                    final Subject<TOutput, TOutput> feed2 = createFeedbackSubject2(null, pMaxRecursion);
                    final Observable<TInput> zipit = pFeedbackValue.call(src, feed2);
                    final Observable<TOutput> pOutputObservable = pFunc1.call(zipit);

                    public Subscription onSubscribe(final Observer<? super TOutput> pOutputObserver) {
                        final Subscription lInnerSubscription = pOutputObservable.subscribe(new Observer<TOutput>() {
                            public void onNext(TOutput pTValue) {
                                if (pTValue != EOR) {
                                    pOutputObserver.onNext(pTValue);
                                    feed2.onNext(pTValue);
                                } else {
                                    onCompleted();
                                }
                            }

                            public void onCompleted() {
                                pOutputObserver.onCompleted();
                                feed2.onCompleted();
                            }

                            public void onError(Throwable pThrowable) {
                                pOutputObserver.onError(pThrowable);
                                feed2.onError(pThrowable);
                            }
                        });
                        return new Subscription() {
                            public void unsubscribe() {
                                lInnerSubscription.unsubscribe();
                            }
                        };
                    }
                });
            }
        });
    }

}
