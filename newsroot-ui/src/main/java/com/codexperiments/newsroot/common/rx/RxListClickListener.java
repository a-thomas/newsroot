package com.codexperiments.newsroot.common.rx;

import rx.Observable;
import rx.subjects.PublishSubject;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

public class RxListClickListener<TView extends View, TItem> implements OnClickListener {
    private final ListView mListView;
    private final Class<TView> mViewClass;
    private final Class<TItem> mItemClass;
    private final PublishSubject<ListEvent<TView, TItem>> mSubject = PublishSubject.create();

    public static <TView extends View, TItem> //
    RxListClickListener<TView, TItem> create(ListView pListView, Class<TView> pViewClass, Class<TItem> pItemClass)
    {
        return new RxListClickListener<TView, TItem>(pListView, pViewClass, pItemClass);
    }

    protected RxListClickListener(ListView pListView, Class<TView> pViewClass, Class<TItem> pItemClass) {
        mListView = pListView;
        mViewClass = pViewClass;
        mItemClass = pItemClass;
    }

    public Observable<ListEvent<TView, TItem>> onClick() {
        return mSubject;
    }

    @Override
    public void onClick(View pView) {
        TView lView = getItemView(pView);
        if (lView != null) {
            int lPosition = getItemPosition(lView);
            if (lPosition != AdapterView.INVALID_POSITION) {
                TItem lItem = getItem(lPosition);
                if (lItem != null) {
                    mSubject.onNext(new ListEvent<TView, TItem>(lPosition, lView, lItem));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private TView getItemView(View pView) {
        // If there is a ClassCastException, that means that the view wasn't an item from the list.
        View lListItem = pView;
        View lView = (View) pView.getParent();
        while (lView != mListView) {
            lListItem = lView;
            lView = (View) lListItem.getParent();
        }

        if (mViewClass.isInstance(lListItem)) {
            return (TView) lListItem;
        } else {
            return null;
        }
    }

    private int getItemPosition(TView pItemView) {
        // If there is a NullPointerException, that means that the item view wasn't attached to the list (e.g. during recycling).
        final int lChildCount = mListView.getChildCount();
        for (int i = 0; i < lChildCount; ++i) {
            if (mListView.getChildAt(i) == pItemView) {
                return mListView.getFirstVisiblePosition() + i;
            }
        }
        return AdapterView.INVALID_POSITION;
    }

    @SuppressWarnings("unchecked")
    private TItem getItem(int pPosition) {
        Object lItem = mListView.getItemAtPosition(pPosition);
        if (mItemClass.isInstance(lItem)) {
            return (TItem) lItem;
        } else {
            return null;
        }
    }
}
