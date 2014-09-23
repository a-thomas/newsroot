package com.codexperiments.newsroot.common.rx;

import rx.Observer;
import android.view.View;
import android.widget.ListView;
import rx.functions.Action2;
import rx.functions.Func1;

public class ListProperty<TItem, TInnerView> implements Observer<TItem> {
    private ListView mListView;
    @SuppressWarnings("rawtypes")
    private Func1/* <? extends View, ? extends TInnerView> */mGetView;
    private Action2<TItem, TInnerView> mApply;

    public static <TItem, TInnerView> ListProperty<TItem, TInnerView> create(ListView pListView,
                                                                             Func1<? extends View, ? extends TInnerView> pGetView,
                                                                             Action2<TItem, TInnerView> pApply)
    {
        return new ListProperty<TItem, TInnerView>(pListView, pGetView, pApply);
    }

    protected ListProperty(ListView pListView,
                           Func1<? extends View, ? extends TInnerView> pGetView,
                           Action2<TItem, TInnerView> pApply)
    {
        // final Func1<TView, View> pGetView
        mListView = pListView;
        mGetView = pGetView;
        mApply = pApply;
    }

    @SuppressWarnings("unchecked")
    protected <TItemView extends View> TItemView findItem(TItem pItem) {
        int lFirstPosition = mListView.getFirstVisiblePosition();
        int lLastPosition = mListView.getLastVisiblePosition();
        for (int lPosition = lFirstPosition; lPosition <= lLastPosition; ++lPosition) {
            if (pItem == mListView.getItemAtPosition(lPosition)) {
                return (TItemView) mListView.getChildAt(lPosition - lFirstPosition);
            }
        }
        return null;
    }

    // public void set(TItem pValue) {
    // }

    @Override
    @SuppressWarnings("unchecked")
    public void onNext(TItem pValue) {
        View lItemView = findItem(pValue);
        TInnerView lInnerView = (TInnerView) mGetView.call(lItemView);
        mApply.call(pValue, lInnerView);
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onError(Throwable pThrowable) {
    }
}
