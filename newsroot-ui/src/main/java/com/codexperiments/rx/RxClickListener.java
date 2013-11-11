package com.codexperiments.rx;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.util.functions.Func1;
import android.view.View;
import android.view.View.OnClickListener;

public class RxClickListener<TView extends View> implements OnClickListener {
    private final Class<TView> mViewClass;
    private final PublishSubject<TView> mSubject = PublishSubject.create();
    private final Func1<View, TView> mFunction;

    // public static <TView extends View> RxClickListener<TView> create(Class<TView> pViewClass) {
    // return new RxClickListener<TView>(pViewClass, null);
    // }

    public static <TView extends View> RxClickListener<TView> create(Func1<View, TView> pFunction) {
        return new RxClickListener<TView>(null, pFunction);
    }

    protected RxClickListener(Class<TView> pViewClass, Func1<View, TView> pFunction) {
        mViewClass = pViewClass;
        mFunction = pFunction;
    }

    public Observable<TView> onClick() {
        return mSubject;
    }

    public Observable<TView> onClick(Func1<View, TView> pFunc) {
        return mSubject.map(pFunc);
    }

    @Override
    public void onClick(View pView) {
        TView lView;
        if (mFunction != null) {
            lView = mFunction.call(pView);
        } else {
            lView = (mViewClass.isInstance(pView)) ? mViewClass.cast(pView) : null;
        }

        if (lView != null) {
            mSubject.onNext(mViewClass.cast(lView));
        }
    }

    // @SuppressWarnings("unchecked")
    // private TView getItemView(View pView) {
    // // If there is a ClassCastException, that means that the view wasn't an item from the list.
    // View lListItem = pView;
    // View lView = (View) pView.getParent();
    // while (lView != mListView) {
    // lListItem = lView;
    // lView = (View) lListItem.getParent();
    // }
    //
    // if (mViewClass.isInstance(lListItem)) {
    // return (TView) lListItem;
    // } else {
    // return null;
    // }
    // }
    //
    // private int getItemPosition(TView pItemView) {
    // // If there is a NullPointerException, that means that the item view wasn't attached to the list (e.g. during recycling).
    // final int lChildCount = mListView.getChildCount();
    // for (int i = 0; i < lChildCount; ++i) {
    // if (mListView.getChildAt(i) == pItemView) {
    // return mListView.getFirstVisiblePosition() + i;
    // }
    // }
    // return AdapterView.INVALID_POSITION;
    // }
    //
    // @SuppressWarnings("unchecked")
    // private TItem getItem(int pPosition) {
    // Object lItem = mListView.getItemAtPosition(pPosition);
    // if (mItemClass.isInstance(lItem)) {
    // return (TItem) lItem;
    // } else {
    // return null;
    // }
    // }
}
