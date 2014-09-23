package com.codexperiments.rx;

import android.util.Log;
import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;

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
     * TODO Use startWith()
     * 
     * @param pMaxRecursion Feedback loop will be performed at most pMaxRecursion before completion.
     * @param pSource
     * @param pFeedbackFactory
     * @param pOutputFactory
     * @return Observable
     */
    public static <TInput, TOutput> Observable<TOutput> feedback(final int pMaxRecursion,
                                                                 final Observable<TInput> pSource,
                                                                 final FeedbackFunc<TInput, TOutput> pFeedbackFactory,
                                                                 final FeedbackOutput<TInput, TOutput> pOutputFactory)
    {
        // Since feedback loop uses a subject, need to defer creation to avoid side-effects between subscriptions.
        // It wouldn't be nice if each subscription fed back others.
        return Observable.defer(new Func0<Observable</*? extends */TOutput>>() {
            public Observable</*? extends */TOutput> call() {
                // Subject allows us to create the feedback loop. Limit the number of possible feedback loops if needed (C).
                final Subject<TOutput, TOutput> lFeedbackSubject = BehaviorSubject.create((TOutput) null);
                final Observable<TOutput> lFeedback = /*
                                                       * (pMaxRecursion > 0) ? lFeedbackSubject.take(pMaxRecursion) :
                                                       */lFeedbackSubject;
                // Merge the input and feedback observables (A).
                final Observable<TInput> lMergeInput = pFeedbackFactory.call(pSource, lFeedback);
                // Output observable right after the feedback "fork" (B).
                final Observable<TOutput> lFinalOutput = pOutputFactory.call(lMergeInput);
                return Observable.create(new OnSubscribeFunc<TOutput>() {

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
                                Log.e("XXXXX", "RRR", pThrowable);
                                pOutputObserver.onError(pThrowable);
                                lFeedbackSubject.onError(pThrowable);
                            }
                        });
                        return new Subscription() {
                            public void unsubscribe() {
                                lInnerSubscription.unsubscribe();
                            }

                            @Override
                            public boolean isUnsubscribed() {
                                return lInnerSubscription.isUnsubscribed();
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

    public static <TInput, TOutput> Observable<TOutput> feedback(final int pMaxRecursion,
                                                                 final FeedbackFunc<TInput, TOutput> pFeedbackFactory,
                                                                 final FeedbackOutput<TInput, TOutput> pOutputFactory)
    {
        return feedback(pMaxRecursion, Observable.from((TInput) null), pFeedbackFactory, pOutputFactory);
    }

    public interface FeedbackFunc<TInput, TOutput> extends Func2<Observable<TInput>, Observable<TOutput>, Observable<TInput>> {
    }

    public interface FeedbackOutput<TInput, TOutput> extends Func1<Observable<TInput>, Observable<TOutput>> {
    }

    public static <TValue> Observable<TValue> takeWhileInclusive(final Observable<TValue> pValues, final Func1<TValue, Boolean> pPredicate) {
        return Observable.create(new OnSubscribeFunc<TValue>() {
            public Subscription onSubscribe(final Observer<? super TValue> pObserver) {
//            final SafeObservableSubscription subscription = new SafeObservableSubscription();
            return /*subscription.wrap(*/pValues.subscribe(new Observer<TValue>() {
                public void onNext(TValue pValue) {
                    pObserver.onNext(pValue);
                    if (pPredicate.call(pValue) == Boolean.FALSE) {
                        pObserver.onCompleted();
                        //subscription.unsubscribe();
                    }
                }

                public void onCompleted() {
                    pObserver.onCompleted();
                }

                public void onError(Throwable pThrowable) {
                    pObserver.onError(pThrowable);
                }
            });//);
            }
        });
    }
}
