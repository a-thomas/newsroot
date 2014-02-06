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

public class OperationFeedback {
    private static <TInput, TOutput> FeedbackObservable<TInput, TOutput> createFeedbackSubject(final Func1<TOutput, TInput> pFeedbackValue,
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

        return new FeedbackObservable<TInput, TOutput>(lOnSubscribe) {
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

    public static <TInput, TOutput> Observable<TOutput> feedback(final Func1<TOutput, TInput> pFeedbackValue,
                                                             final int pMaxRecursion,
                                                             final Func1<Observable<TInput>, Observable<TOutput>> pFunc1)
    {
        return Observable.defer(new Func0<Observable<? extends TOutput>>() {
            public Observable<? extends TOutput> call() {
                return Observable.create(new OnSubscribeFunc<TOutput>() {
                    final FeedbackObservable<TInput, TOutput> feed1 = createFeedbackSubject(pFeedbackValue, pMaxRecursion);
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

    public abstract static class FeedbackObservable<TInput, TOutput> extends Subject<TOutput, TInput> {

        protected FeedbackObservable(OnSubscribeFunc<TInput> onSubscribe) {
            super(onSubscribe);
        }

        // public Observable<TOutput> feed(final Observable<TOutput> pOutputObservable) {
        // // return Observable.defer(new Func0<Observable<TOutput>>() {
        // // public Observable<TOutput> call() {
        // // return Observable.create(new OnSubscribeFunc<TOutput>() {
        // // public Subscription onSubscribe(final Observer<? super TOutput> pOutputObserver) {
        // // final Subscription lInnerSubscription = pOutputObservable.subscribe(new Observer<TOutput>() {
        // // public void onNext(TOutput pTValue) {
        // // pOutputObserver.onNext(pTValue);
        // // this.onNext(pTValue);
        // // }
        // //
        // // public void onCompleted() {
        // // pOutputObserver.onCompleted();
        // // this.onCompleted();
        // // }
        // //
        // // public void onError(Throwable pThrowable) {
        // // pOutputObserver.onError(pThrowable);
        // // this.onError(pThrowable);
        // // }
        // // });
        // // return new Subscription() {
        // // public void unsubscribe() {
        // // lInnerSubscription.unsubscribe();
        // // }
        // // };
        // // }
        // // });
        // // }
        // // });
        //
        // return Observable.create(new OnSubscribeFunc<TOutput>() {
        // public Subscription onSubscribe(final Observer<? super TOutput> pOutputObserver) {
        // final Subscription lInnerSubscription = pOutputObservable.subscribe(new Observer<TOutput>() {
        // public void onNext(TOutput pTValue) {
        // pOutputObserver.onNext(pTValue);
        // this.onNext(pTValue);
        // }
        //
        // public void onCompleted() {
        // pOutputObserver.onCompleted();
        // this.onCompleted();
        // }
        //
        // public void onError(Throwable pThrowable) {
        // pOutputObserver.onError(pThrowable);
        // this.onError(pThrowable);
        // }
        // });
        // return new Subscription() {
        // public void unsubscribe() {
        // lInnerSubscription.unsubscribe();
        // }
        // };
        // }
        // });
        // }
    }

    // public static <TInput, TOutput> FeedbackObservable<TInput, TOutput> feed(final Func1<TOutput, TInput> pFeedbackValue,
    // final int pMaxRecursion)
    // {
    // Func0<Observable<TOutput>> lObservableFactory = new Func0<Observable<TOutput>>() {
    // public Observable<TOutput> call() {
    // final Subject<TOutput, TInput> lFeedbackSubject = createFeedbackSubject(pFeedbackValue, pMaxRecursion);
    //
    // return Observable.create(new OnSubscribeFunc<TOutput>() {
    // public Subscription onSubscribe(final Observer<? super TOutput> pOutputObserver) {
    // final Subscription lInnerSubscription = pObservable.subscribe(new Observer<TOutput>() {
    // public void onNext(TOutput pTValue) {
    // pOutputObserver.onNext(pTValue);
    // lFeedbackSubject.onNext(pTValue);
    // }
    //
    // public void onCompleted() {
    // pOutputObserver.onCompleted();
    // lFeedbackSubject.onCompleted();
    // }
    //
    // public void onError(Throwable pThrowable) {
    // pOutputObserver.onError(pThrowable);
    // lFeedbackSubject.onError(pThrowable);
    // }
    // });
    // return new Subscription() {
    // public void unsubscribe() {
    // lInnerSubscription.unsubscribe();
    // }
    // };
    // }
    // });
    // }
    // };
    // return new FeedbackObservable<TInput, TOutput>(OperationDefer.defer(lObservableFactory)) {
    // @Override
    // public Subscription feed(Observable<TOutput> pOutputObservable) {
    // return null;
    // }
    // };
    // }

}
