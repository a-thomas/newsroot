//package com.codexperiments.newsroot.common.rx;
//
//import rx.Observable;
//import rx.Observer;
//import rx.Subscription;
//import rx.functions.Action1;
//import rx.subjects.PublishSubject;
//
//public class Property<TValue> extends Observable<TValue> implements Observer<TValue> {
//    public static <TInput> Action1<TInput> toggle(final Property<Boolean> pProperty) {
//        return new Action1<TInput>() {
//            public void call(TInput pInput) {
//                Boolean lValue = Boolean.valueOf(!pProperty.mProxy.get().booleanValue());
//                pProperty.set(lValue);
//            }
//        };
//    }
//
//    public interface PropertyAccess<TValue> {
//        TValue get();
//
//        void set(TValue pValue);
//    }
//
//    private PropertyAccess<TValue> mProxy;
//    private PublishSubject<TValue> mProperty;
//
//    public static <TValue> Property<TValue> create(PropertyAccess<TValue> pProxy) {
//        return new Property<TValue>(pProxy);
//    }
//
//    protected Property(PropertyAccess<TValue> pProxy) {
//        super(null);
//        mProxy = pProxy;
//        // mProperty = BehaviorSubject.createWithDefaultValue(mProxy.get());
//        mProperty = PublishSubject.create();
//    }
//
//    public void reset() {
//        mProperty.onNext(mProxy.get());
//    }
//
//    public void reset(PropertyAccess<TValue> pPropertyAccess) {
//        mProxy = pPropertyAccess;
//        mProperty.onNext(mProxy.get());
//    }
//
//    public void set(TValue pValue) {
//        mProxy.set(pValue);
//        mProperty.onNext(pValue);
//    }
//
//    public void setIfNew(TValue pValue) {
//        TValue lValue = mProxy.get();
//        if ((lValue == null && pValue != null) || ((lValue != null) && !lValue.equals(pValue))) {
//            mProxy.set(pValue);
//            mProperty.onNext(pValue);
//        }
//    }
//
//    @Override
//    public Subscription subscribe(Observer<? super TValue> pObserver) {
//        return mProperty.subscribe(pObserver);
//    }
//
//    @Override
//    public void onNext(TValue pValue) {
//        mProxy.set(pValue);
//        mProperty.onNext(pValue);
//    }
//
//    @Override
//    public void onCompleted() {
//        mProperty.onCompleted();
//    }
//
//    @Override
//    public void onError(Throwable pThrowable) {
//        mProperty.onError(pThrowable);
//    }
//}
