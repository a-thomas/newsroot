package com.codexperiments.rx;

import rx.util.functions.Func1;

public class RxUtils {
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
}
