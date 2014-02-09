package com.codexperiments.rx;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import rx.util.functions.Func0;
import rx.util.functions.Func1;
import rx.util.functions.Func2;

public class Rxt {
    public static <TValue> Func1<TValue, Boolean> nullValue() {
        return new Func1<TValue, Boolean>() {
            public Boolean call(TValue pValue) {
                return Boolean.valueOf(pValue == null);
            }
        };
    }

    public static <TValue> Func1<TValue, Boolean> notNullValue() {
        return new Func1<TValue, Boolean>() {
            public Boolean call(TValue pValue) {
                return Boolean.valueOf(pValue != null);
            }
        };
    }

    /**
     * Creates a feedback loop inside an observable pipeline.
     * 
     * <pre>
     *               (A)        (B)
     * Source +------>+---...--->+------>+ Output
     *                ^          |
     *                | Feedback |
     *                +<---------+
     *                     (C)
     * </pre>
     * 
     * @param pMaxRecursion Feedback loop will be performed at most pMaxRecursion before completion.
     * @param pSource
     * @param pFeedbackFactory
     * @param pOutputFactory
     * @return
     */
    public static <TInput, TOutput> Observable<TOutput> feedback(final int pMaxRecursion,
                                                                 final Observable<TInput> pSource,
                                                                 final FeedbackFunc<TInput, TOutput> pFeedbackFactory,
                                                                 final FeedbackOutput<TInput, TOutput> pOutputFactory)
    {
        // Since feedback loop uses a subject, need to defer creation to avoid side-effects between subscriptions.
        // It wouldn't be nice if each subscription fed back others.
        return Observable.defer(new Func0<Observable<? extends TOutput>>() {
            public Observable<? extends TOutput> call() {
                return Observable.create(new OnSubscribeFunc<TOutput>() {
                    // Subject allows us to create the feedback loop. Limit the number of possible feedback loops if needed (C).
                    Subject<TOutput, TOutput> lFeedbackSubject = BehaviorSubject.create((TOutput) null);
                    Observable<TOutput> lFeedback = (pMaxRecursion > 0) ? lFeedbackSubject.take(pMaxRecursion) : lFeedbackSubject;
                    // Merge the input and feedback observables (A).
                    Observable<TInput> lMergeInput = pFeedbackFactory.call(pSource, lFeedback);
                    // Output observable right after the feedback "fork" (B).
                    Observable<TOutput> lFinalOutput = pOutputFactory.call(lMergeInput);

                    public Subscription onSubscribe(final Observer<? super TOutput> pOutputObserver) {
                        final Subscription lInnerSubscription = lFinalOutput.subscribe(new Observer<TOutput>() {
                            public void onNext(TOutput pTValue) {
                                pOutputObserver.onNext(pTValue);
                                lFeedbackSubject.onNext(pTValue);
                            }

                            public void onCompleted() {
                                pOutputObserver.onCompleted();
                                lFeedbackSubject.onCompleted();
                            }

                            public void onError(Throwable pThrowable) {
                                pOutputObserver.onError(pThrowable);
                                lFeedbackSubject.onError(pThrowable);
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

    public static <TInput, TOutput> Observable<TOutput> feedback(final int pMaxRecursion,
                                                                 final TInput pInitialValue,
                                                                 final FeedbackFunc<TInput, TOutput> pFeedbackFactory,
                                                                 final FeedbackOutput<TInput, TOutput> pOutputFactory)
    {
        return feedback(pMaxRecursion, Observable.from(pInitialValue), pFeedbackFactory, pOutputFactory);
    }

    public interface FeedbackFunc<TInput, TOutput> extends Func2<Observable<TInput>, Observable<TOutput>, Observable<TInput>> {
    }

    public interface FeedbackOutput<TInput, TOutput> extends Func1<Observable<TInput>, Observable<TOutput>> {
    }
}
