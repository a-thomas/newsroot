package com.codexperiments.rx;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

import com.codexperiments.newsroot.common.rx.RxField;

public class RxProperty<TItem> extends Observable<TItem> implements Observer<TItem> {
    private final PublishSubject<TItem> mSource;
    // Updated field, changed each time a new value is sent through the Rx pipeline. Since source uses an immediate scheduler,
    // field can be stored safely as a member to optimize code. Else we would have to sent it through the Rx pipeline too.
    private RxField mField;

    public static <TItem> RxProperty<TItem> create() {
        return new RxProperty<TItem>();
    }

    protected RxProperty() {
        super(null);
        mSource = PublishSubject.create();
        mField = null;
    }

    @Override
    public Subscription subscribe(Observer<? super TItem> pObserver) {
        return mSource.subscribe(pObserver);
    }

    public Observable<TItem> whenAny(final RxField... pMatchingFields) {
        return Observable.create(new OnSubscribeFunc<TItem>() {
            public Subscription onSubscribe(final Observer<? super TItem> pItemObserver) {
                return mSource.subscribe(new Observer<TItem>() {
                    public void onNext(TItem pItem) {
                        if ((mField == null) || matches(mField, pMatchingFields)) {
                            pItemObserver.onNext(pItem);
                        }
                    }

                    public void onCompleted() {
                        pItemObserver.onCompleted();
                    }

                    public void onError(Throwable pThrowable) {
                        pItemObserver.onError(pThrowable);
                    }
                });
            }
        });
    }

    public void notify(TItem pItem) {
        mField = null;
        mSource.onNext(pItem);
    }

    public void notify(TItem pItem, RxField pField) {
        mField = pField;
        mSource.onNext(pItem);
    }

    @Override
    public void onNext(TItem pItem) {
        mField = null;
        mSource.onNext(pItem);
    }

    @Override
    public void onCompleted() {
        mSource.onCompleted();
    }

    @Override
    public void onError(Throwable pThrowable) {
        mSource.onError(pThrowable);
    }

    private static boolean matches(RxField pField, RxField[] pMatchingFields) {
        for (RxField lMatchingField : pMatchingFields) {
            if (pField == lMatchingField) return true;
        }
        return false;
    }
}
