package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class RxTodoProperty<TItem> implements Observer<TItem> {
    private PublishSubject<Event<TItem>> mSource;

    public static <TItem> RxTodoProperty<TItem> create() {
        return new RxTodoProperty<TItem>();
    }

    protected RxTodoProperty() {
        mSource = PublishSubject.create();
    }

    @Override
    public void onNext(TItem pItem) {
        mSource.onNext(new Event<TItem>(pItem, null));
    }

    @Override
    public void onCompleted() {
        mSource.onCompleted();
    }

    @Override
    public void onError(Throwable pThrowable) {
        mSource.onError(pThrowable);
    }

    public Observable<TItem> whenAnyOrWhole(final RxField pField) {
        return Observable.create(new OnSubscribeFunc<TItem>() {
            public Subscription onSubscribe(final Observer<? super TItem> pItemObserver) {
                return mSource.subscribe(new Observer<Event<TItem>>() {
                    public void onNext(Event<TItem> pItem) {
                        RxField lField = pItem.getField();
                        if ((lField == null) || (pField == lField)) {
                            pItemObserver.onNext(pItem.getItem());
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

    public void update(TItem pItem, RxField pField) {
        mSource.onNext(new Event<TItem>(pItem, pField));
    }

    public void update(TItem pItem) {
        mSource.onNext(new Event<TItem>(pItem, null));
    }

    private static class Event<TItem> {
        private TItem mItem;
        private RxField mField;

        public Event(TItem pItem, RxField pField) {
            super();
            mItem = pItem;
            mField = pField;
        }

        public TItem getItem() {
            return mItem;
        }

        public RxField getField() {
            return mField;
        }
    }
}
