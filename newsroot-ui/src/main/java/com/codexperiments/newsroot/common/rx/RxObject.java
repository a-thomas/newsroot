package com.codexperiments.newsroot.common.rx;

import rx.subjects.PublishSubject;

public class RxObject {
    private PublishSubject<RxChange> mChangeObservable;

    public PublishSubject<RxChange> getChangeObservable() {
        return mChangeObservable;
    }

    public void setChangesObservable(PublishSubject<RxChange> pChangeObservable) {
        mChangeObservable = pChangeObservable;
    }
}
