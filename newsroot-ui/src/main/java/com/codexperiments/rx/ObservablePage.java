package com.codexperiments.rx;

import rx.Observable;
import rx.util.BufferClosing;
import rx.util.functions.Func0;

public class ObservablePage<T> {
    private Observable<T> mObservable;
    private Func0<Observable<BufferClosing>> mController;

    public static <T> ObservablePage<T> create(Observable<T> pObservable, final Observable<BufferClosing> pController) {
        return new ObservablePage<T>(pObservable, new Func0<Observable<BufferClosing>>() {
            public Observable<BufferClosing> call() {
                return pController;
            }
        });
    }

    public static <T> ObservablePage<T> create(Observable<T> pObservable, ObservablePage<?> pObservablePage) {
        return new ObservablePage<T>(pObservable, pObservablePage.mController);
    }

    public ObservablePage(Observable<T> pObservable, Func0<Observable<BufferClosing>> pController) {
        super();
        mObservable = pObservable;
        mController = pController;
    }

    public Observable<T> observable() {
        return mObservable;
    }

    public Func0<Observable<BufferClosing>> controller() {
        return mController;
    }
}
