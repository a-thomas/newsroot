package com.codexperiments.newsroot.common.rx;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;
import android.view.View;
import android.widget.ListView;
import rx.functions.Action2;

public class RxListProperty<TView extends View, TItem> implements Observer<ListEvent<TView, TItem>> {
    private final ListView mListView;
    private final Class<TView> mViewClass;
    private final Class<TItem> mItemClass;
    private List<Action2<? super TView, ? super TItem>> mActions;

    public static <TView extends View, TItem> //
    RxListProperty<TView, TItem> create(ListView pListView, Class<TView> pViewClass, Class<TItem> pItemClass)
    {
        return new RxListProperty<TView, TItem>(pListView, pViewClass, pItemClass);
    }

    protected RxListProperty(ListView pListView, Class<TView> pViewClass, Class<TItem> pItemClass) {
        mListView = pListView;
        mViewClass = pViewClass;
        mItemClass = pItemClass;
        mActions = new ArrayList<Action2<? super TView, ? super TItem>>();
    }

    public RxListProperty<TView, TItem> register(Action2<? super TView, ? super TItem> pAction) {
        mActions.add(pAction);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onNext(ListEvent<TView, TItem> pEvent) {
        if (mViewClass.isInstance(pEvent.getView()) && mItemClass.isInstance(pEvent.getItem())) {
            TItem lItem = pEvent.getItem();
            TView lView = pEvent.getView();
            for (Action2<? super TView, ? super TItem> lAction : mActions) {
                lAction.call(lView, lItem);
            }
        }
        // if (mItemClass.isInstance(pItem)) {
        // TItem lItem = (TItem) pItem;
        // TView lView = getItemView(lItem);
        // if (lView != null) {
        // for (Action2<? super TView, ? super TItem> lAction : mActions) {
        // lAction.call(lView, lItem);
        // }
        // }
        // }
    }

    @SuppressWarnings("unchecked")
    public void onNext(View pView, Object pItem) {
        if (mViewClass.isInstance(pView) && mItemClass.isInstance(pItem)) {
            TItem lItem = (TItem) pItem;
            TView lView = (TView) pView;
            for (Action2<? super TView, ? super TItem> lAction : mActions) {
                lAction.call(lView, lItem);
            }
        }
    }

    @Override
    public void onError(Throwable pThrowable) {
    }

    @Override
    public void onCompleted() {
    }

    @SuppressWarnings("unchecked")
    private TView getItemView(TItem pItem) {
        int lFirstPosition = mListView.getFirstVisiblePosition();
        int lLastPosition = mListView.getLastVisiblePosition();
        for (int lPosition = lFirstPosition; lPosition <= lLastPosition; ++lPosition) {
            Object lItem = mListView.getItemAtPosition(lPosition);
            if (pItem == lItem) {
                TView lView = (TView) mListView.getChildAt(lPosition - lFirstPosition);
                // This check is just a security
                if (mViewClass.isInstance(lView)) {
                    return lView;
                }
            }
        }
        return null;
    }
}
