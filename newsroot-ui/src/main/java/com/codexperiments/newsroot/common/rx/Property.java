package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.BehaviorSubject;

public class Property<TValue> extends Observable<TValue> implements Observer<TValue> {
    protected TValue mValue;
    protected BehaviorSubject<TValue> mProperty;

    public static <TParam> Property<TParam> create(TParam pValue) {
        return new Property<TParam>(pValue);
    }

    protected Property(TValue pValue) {
        super(null);
        mValue = pValue;
        mProperty = BehaviorSubject.createWithDefaultValue(pValue);
    }
    
    public TValue get() {
        return mValue;
    }
    
    public void set(TValue pValue) {
        mValue = pValue;
        mProperty.onNext(pValue);
    }

    @Override
    public Subscription subscribe(Observer<? super TValue> pObserver) {
        return mProperty.subscribe(pObserver);
    }

    @Override
    public void onNext(TValue pValue) {
        mValue = pValue;
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
