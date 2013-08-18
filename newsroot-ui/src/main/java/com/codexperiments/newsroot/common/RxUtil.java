package com.codexperiments.newsroot.common;

import rx.Observable;
import rx.util.functions.Func1;

public class RxUtil {
    public static <T, R> Observable<R> downcast(Observable<T> pObservable) {
        return pObservable.map(new Func1<T, R>() {
            @SuppressWarnings("unchecked")
            public R call(T pT) {
                return (R) pT;
            }
        });
    }

    public static <T, R> Observable<R> downcast(Observable<T> pObservable, final Class<R> pDowncastedType) {
        return pObservable.map(new Func1<T, R>() {
            public R call(T pT) {
                return pDowncastedType.cast(pT);
            }
        });
    }
}