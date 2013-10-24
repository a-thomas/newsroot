package com.codexperiments.newsroot.common.structure;

import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import com.codexperiments.newsroot.common.Page;

public class RxPageIndex<TItem> implements PageIndex<TItem> {
    private PageIndex<TItem> mIndex;
    private PublishSubject<Page<? extends TItem>> mInsertObservable = PublishSubject.create();

    public static <TItem> RxPageIndex<TItem> newPageIndex() {
        return new RxPageIndex<TItem>(new TreePageIndex<TItem>());
    }

    public static <TItem> RxPageIndex<TItem> newPageIndex(TreePageIndex<TItem> pPageIndex) {
        return new RxPageIndex<TItem>(pPageIndex);
    }

    public RxPageIndex(PageIndex<TItem> pIndex) {
        super();
        mIndex = pIndex;
    }

    public Observable<Page<? extends TItem>> onInsert() {
        return mInsertObservable;
    }

    public List<TItem> find(int pIntervalIndex, int pIntervalSize) {
        return mIndex.find(pIntervalIndex, pIntervalSize);
    }

    public void insert(Page<? extends TItem> pPage) {
        mIndex.insert(pPage);
        mInsertObservable.onNext(pPage);
    }

    public int size() {
        return mIndex.size();
    }
}