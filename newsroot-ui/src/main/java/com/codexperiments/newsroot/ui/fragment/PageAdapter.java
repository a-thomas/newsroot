package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Action1;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.domain.twitter.News;

public abstract class PageAdapter<TItem> extends BaseAdapter {
    private RxRecycleCallback<TItem> mRecycleCallback;
    private RefreshCallback mRefreshCallback;
    private MoreCallback mMoreCallback;

    private PageIndex<News> mIndex;
    private int mIndexSize;
    private int mLastItemSeen;

    public PageAdapter(PageIndex<News> pIndex) {
        super();
        mRecycleCallback = null;
        mRefreshCallback = null;
        mMoreCallback = null;

        mIndex = pIndex;
        mLastItemSeen = -1;
        mIndexSize = 0;
    }

    public boolean isLastItem(int pPosition) {
        return (pPosition == getCount() - 1);
    }

    @Override
    public void notifyDataSetChanged() {
        mIndexSize = mIndex.size();
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        if (isLastItem(pPosition) && (pPosition > mLastItemSeen)) {
            if (mMoreCallback != null) {
                mMoreCallback.onMore();
            }
            mLastItemSeen = pPosition;
        }
        return null;
    }

    @Override
    public int getCount() {
        return mIndexSize;
    }

    @Override
    public Object getItem(int pPosition) {
        return mIndex.find(mIndexSize - pPosition - 1, 1).get(0);
    }

    public void doRecycly(TItem pItem, View pView) {
        mRecycleCallback.onRecycle(pItem, pView);
    }

    public void setRecycleCallback(RxRecycleCallback<TItem> pRecycleCallback) {
        mRecycleCallback = pRecycleCallback;
    }

    public void setRefreshCallback(RefreshCallback pRefreshCallback) {
        mRefreshCallback = pRefreshCallback;
    }

    public void setMoreCallback(MoreCallback pMoreCallback) {
        mMoreCallback = pMoreCallback;
    }

    public interface RxRecycleCallback<TItem> {
        void onRecycle(TItem pItem, View pView);

        Observable<TItem> toItems();

        Observable<View> toViews();

        Subscription subscribe(Observer<TItem> pObserver);

        Subscription subscribe(Action1<TItem> pAction);
    }

    public interface RefreshCallback {
        void onRefresh();
    }

    public interface MoreCallback {
        void onMore();
    }
}
