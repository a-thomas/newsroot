package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;
import rx.util.functions.Func1;

public class Property2<TValue> extends Observable<TValue> implements Observer<TValue> {
    public static Func1<Void, Boolean> toggle(final Property2<Boolean> pProperty) {
        return new Func1<Void, Boolean>() {
            public Boolean call(Void pVoid) {
                return Boolean.valueOf(!pProperty.mProxy.get());
            }
        };
    }

    public interface PropertyProxy<TValue> {
        TValue get();

        void set(TValue pValue);
    }

    private PropertyProxy<TValue> mProxy;
    private BehaviorSubject<TValue> mProperty;

    public static <TValue> Property2<TValue> create(PropertyProxy<TValue> pProxy) {
        return new Property2<TValue>(pProxy);
    }

    protected Property2(PropertyProxy<TValue> pProxy) {
        super(null);
        mProxy = pProxy;
        mProperty = BehaviorSubject.createWithDefaultValue(mProxy.get());
    }

    public void set(TValue pValue) {
        mProxy.set(pValue);
        mProperty.onNext(pValue);
    }

    public void setIfNew(TValue pValue) {
        TValue lValue = mProxy.get();
        if ((lValue == null && pValue != null) || ((lValue != null) && !lValue.equals(pValue))) {
            mProxy.set(pValue);
            mProperty.onNext(pValue);
        }
    }

    @Override
    public Subscription subscribe(Observer<? super TValue> pObserver) {
        return mProperty.subscribe(pObserver);
    }

    @Override
    public void onNext(TValue pValue) {
        mProxy.set(pValue);
        mProperty.onNext(pValue);
    }

    @Override
    public void onCompleted() {
        mProperty.onCompleted();
    }

    @Override
    public void onError(Throwable pThrowable) {
        mProperty.onError(pThrowable);
    }
}
