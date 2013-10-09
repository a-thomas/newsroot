package com.codexperiments.newsroot.ui.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;

public class PageAdapter<TItem> extends BaseAdapter {
    private static final int ITEM_TWEET = 0;
    private static final int ITEM_TIMEGAP = 1;
    private static final int ITEM_MORE = 2;
    
    private LayoutInflater mLayoutInflater;
    private RefreshCallback mRefreshCallback;
    private MoreCallback mMoreCallback;

    private PageIndex<TItem> mIndex;
    private int mLastPosition;
    private int mItemCount;
    private boolean mHasMore;

    public PageAdapter(LayoutInflater pLayoutInflater) {
        super();
        mLayoutInflater = pLayoutInflater;
        mRefreshCallback = null;
        mMoreCallback = null;

        mIndex = null;
        mLastPosition = -1;
        mItemCount = 0;
        mHasMore = true;
    }
    
    public void setHasMore(boolean pValue) {
        mHasMore = pValue;
    }

    public void bindTo(PageIndex<TItem> pIndex) {
        mIndex = pIndex;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        mItemCount = mIndex.size();
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        if ((pPosition >= mItemCount) && (mLastPosition != pPosition) /* && (mTimeline.hasMore()) */) {
            if (mMoreCallback != null) mMoreCallback.onMore();
            mLastPosition = pPosition;
        }

        switch (getItemViewType(pPosition)) {
        case ITEM_MORE:
            return getMoreView(pPosition, pConvertView, pParent);
        case ITEM_TIMEGAP:
            return getTimeGapView(pPosition, pConvertView, pParent);
        case ITEM_TWEET:
        default:
            return getNewsView(pPosition, pConvertView, pParent);
        }
    }
    
    private View getMoreView(int pPosition, View pConvertView, ViewGroup pParent) {
        NewsMoreItem lNewsMoreItem = (NewsMoreItem) pConvertView;
        if (pConvertView == null) {
            lNewsMoreItem = (NewsMoreItem) mLayoutInflater.inflate(R.layout.item_news_more, pParent, false);
        }
        lNewsMoreItem.setContent();
        return lNewsMoreItem;
    }
    
    private View getTimeGapView(int pPosition, View pConvertView, ViewGroup pParent) {
        NewsTimeGapItem lNewsItem = (NewsTimeGapItem) pConvertView;
        if (pConvertView == null) {
            lNewsItem = (NewsTimeGapItem) mLayoutInflater.inflate(R.layout.item_news_timegap, pParent, false);
        }
        lNewsItem.setContent((TimeGap) mIndex.find((mItemCount - 1) - pPosition, 1).get(0));
        return lNewsItem;
    }

    private View getNewsView(int pPosition, View pConvertView, ViewGroup pParent) {
        NewsItem lNewsItem = (NewsItem) pConvertView;
        if (pConvertView == null) {
            lNewsItem = (NewsItem) mLayoutInflater.inflate(R.layout.item_news, pParent, false);
        }
        lNewsItem.setContent((Tweet) mIndex.find((mItemCount - 1) - pPosition, 1).get(0));
        return lNewsItem;
    }

    @Override
    public int getCount() {
        if (mHasMore) return mItemCount + 1;
        else return mItemCount;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public long getItemId(int pPosition) {
        return pPosition;
    }

    @Override
    public Object getItem(int pPosition) {
        return mIndex.find(mItemCount - pPosition - 1, 1).get(0);
    }

    @Override
    public int getItemViewType(int pPosition) {
        if (mHasMore && (pPosition == mItemCount)) return ITEM_MORE;
        else if (getItem(pPosition) instanceof TimeGap) return ITEM_TIMEGAP;
        else return ITEM_TWEET;
    }

//    @Override
//    public boolean isEnabled(int position) {
//        return position % 2 == 0;
//    }

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
