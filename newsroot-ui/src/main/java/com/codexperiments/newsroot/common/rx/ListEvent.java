package com.codexperiments.newsroot.common.rx;

public class ListEvent<TView, TItem> {
    private int mPosition;
    private TView mView;
    private TItem mItem;

    public ListEvent(int pPosition, TView pView, TItem pItem) {
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
