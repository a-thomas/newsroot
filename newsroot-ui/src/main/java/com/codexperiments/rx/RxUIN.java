package com.codexperiments.rx;

import rx.Observer;
import rx.util.functions.Action2;
import rx.util.functions.Func1;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class RxUIN {
    public static <TItem, TView extends View> Observer<TView> fromListViewItem(final ListView pListView,
                                                                               final Class<TItem> pItemClass,
                                                                               final Action2<TItem, TView> pAction)
    {
        return new Observer<TView>() {
            @Override
            public void onNext(TView pView) {
                int lPosition = pListView.getPositionForView(pView);
                if (lPosition != AdapterView.INVALID_POSITION) {
                    Object lItem = pListView.getItemAtPosition(lPosition);
                    if (lItem.getClass() == pItemClass) {
                        pAction.call(pItemClass.cast(lItem), pView);
                    }
                }
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable pThrowable) {
            }
        };
    }

    public static <TItem, TView extends View> Func1<TView, TItem> fromListViewToItem(final ListView pListView,
                                                                                   final Class<TItem> pItemClass)
    {
        return new Func1<TView, TItem>() {
            public TItem call(TView pView) {
                int lPosition = pListView.getPositionForView(pView);
                if (lPosition != AdapterView.INVALID_POSITION) {
                    Object lItem = pListView.getItemAtPosition(lPosition);
                    return pItemClass.cast(lItem);
                } else {
                    return null;
                }
            }
        };
    }

    public static <TItem, TView> Observer<TItem> toListViewItem(final ListView pListView,
                                                                final Class<TView> pViewClass,
                                                                final Action2<TItem, TView> pAction)
    {
        return new Observer<TItem>() {
            @Override
            public void onNext(TItem pItem) {
                // Find if the item view among the visible views.
                int lFirstPosition = pListView.getFirstVisiblePosition();
                int lLastPosition = pListView.getLastVisiblePosition();
                for (int lPosition = lFirstPosition; lPosition <= lLastPosition; ++lPosition) {
                    Object lItem = pListView.getItemAtPosition(lPosition);
                    if (pItem == lItem) {
                        View lView = pListView.getChildAt(lPosition - lFirstPosition);
                        if (lView.getClass() == pViewClass) {
                            pAction.call(pItem, pViewClass.cast(lView));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable pThrowable) {
            }
        };
    }

    public static <TView extends View> Func1<View, TView> convertToListViewItem(final ListView pListView,
                                                                                final Class<TView> pViewClass)
    {
        return new Func1<View, TView>() {
            public TView call(View pView) {
                // If there is a ClassCastException, that means that the view wasn't an item from the list.
                View lListItem = pView;
                View lView = (View) pView.getParent();
                while (lView != pListView) {
                    lListItem = lView;
                    lView = (View) lListItem.getParent();
                }

                if (pViewClass.isInstance(lListItem)) {
                    return pViewClass.cast(lListItem);
                } else {
                    return null;
                }
            }
        };
    }
}
