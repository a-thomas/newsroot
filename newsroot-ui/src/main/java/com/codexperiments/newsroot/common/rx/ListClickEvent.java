package com.codexperiments.newsroot.common.rx;

public class ListClickEvent<TView, TItem> {
    private int mPosition;
    private TView mView;
    private TItem mItem;

    public ListClickEvent(int pPosition, TView pView, TItem pItem) {
        super();
        mPosition = pPosition;
        mView = pView;
        mItem = pItem;
    }

    public int getPosition() {
        return mPosition;
    }

    public TView getView() {
        return mView;
    }

    public TItem getItem() {
        return mItem;
    }
}
