package com.codexperiments.newsroot.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.domain.twitter.News;

public class PageAdapter<TItem> extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    private RefreshCallback mRefreshCallback;
    private MoreCallback mMoreCallback;

    private PageIndex<TItem> mIndex;
    private int mLastPosition;

    public PageAdapter(LayoutInflater pLayoutInflater) {
        super();
        mLayoutInflater = pLayoutInflater;
        mRefreshCallback = null;
        mMoreCallback = null;

        mIndex = null;
        mLastPosition = 0;
    }

    public void bindTo(PageIndex<TItem> pIndex) {
        mIndex = pIndex;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        if ((pPosition == mIndex.size() - 1) && (mLastPosition != pPosition) /* && (mTimeline.hasMore()) */) {
            if (mMoreCallback != null) mMoreCallback.onMore();
            mLastPosition = pPosition;
        }

        NewsItem lUINewsItem;
        if (pConvertView == null) {
            lUINewsItem = (NewsItem) mLayoutInflater.inflate(R.layout.item_news, pParent, false);
        } else {
            lUINewsItem = (NewsItem) pConvertView;
        }

        lUINewsItem.setContent((News) mIndex.find(pPosition, 1).get(0));
        return lUINewsItem;
    }

    @Override
    public int getCount() {
        return (mIndex != null) ? mIndex.size() : 0;
    }

    @Override
    public long getItemId(int pPosition) {
        return pPosition;
    }

    @Override
    public Object getItem(int pPosition) {
        return mIndex.find(pPosition, 1).get(0);
    }

    public void setRefreshCallback(RefreshCallback pRefreshCallback) {
        mRefreshCallback = pRefreshCallback;
    }

    public void setMoreCallback(MoreCallback pMoreCallback) {
        mMoreCallback = pMoreCallback;
    }

    public interface RefreshCallback {
        void onRefresh();
    }

    public interface MoreCallback {
        void onMore();
    }
}

// package com.codexperiments.newsroot.ui.fragment;
//
// import android.view.LayoutInflater;
// import android.view.View;
// import android.view.ViewGroup;
// import android.widget.BaseAdapter;
//
// import com.codexperiments.newsroot.R;
// import com.codexperiments.newsroot.common.Page;
// import com.codexperiments.newsroot.common.structure.PageIndex;
// import com.codexperiments.newsroot.domain.twitter.News;
//
// public class PageAdapter<TItem> extends BaseAdapter {
// private LayoutInflater mLayoutInflater;
// private RefreshCallback mRefreshCallback;
// private MoreCallback mMoreCallback;
//
// private PageIndex<TItem> mIndex;
// private int mLastPosition;
//
// public PageAdapter(LayoutInflater pLayoutInflater) {
// super();
// mLayoutInflater = pLayoutInflater;
// mRefreshCallback = null;
// mMoreCallback = null;
//
// mIndex = null;
// mLastPosition = 0;
// }
//
// public void bindTo(PageIndex<TItem> pIndex) {
// mIndex = pIndex;
// notifyDataSetChanged();
// }
//
// @Override
// public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
// if ((pPosition == mIndex.size() - 1) && (mLastPosition != pPosition) /* && (mTimeline.hasMore()) */) {
// if (mMoreCallback != null) mMoreCallback.onMore();
// mLastPosition = pPosition;
// }
//
// NewsItem lUINewsItem;
// if (pConvertView == null) {
// lUINewsItem = (NewsItem) mLayoutInflater.inflate(R.layout.item_news, pParent, false);
// } else {
// lUINewsItem = (NewsItem) pConvertView;
// }
//
// lUINewsItem.setContent((News) mIndex.find(pPosition, 1).get(0));
// return lUINewsItem;
// }
//
// @Override
// public long getItemId(int pPosition) {
// return pPosition;
// }
//
// @Override
// public Object getItem(int pPosition) {
// return mIndex.find(pPosition, 1).get(0);
// }
//
// @Override
// public int getCount() {
// return mIndex.size();
// }
//
// public void append(Page<? extends TItem> pPage) {
// mIndex.insert(pPage);
// }
//
// public void setRefreshCallback(RefreshCallback pRefreshCallback) {
// mRefreshCallback = pRefreshCallback;
// }
//
// public void setMoreCallback(MoreCallback pMoreCallback) {
// mMoreCallback = pMoreCallback;
// }
//
// public interface RefreshCallback {
// void onRefresh();
// }
//
// public interface MoreCallback {
// void onMore();
// }
// }
