package com.codexperiments.newsroot.ui.fragment;

import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.google.common.collect.Maps;

public class PageAdapter<TItem> extends BaseAdapter {
    private static final int ITEM_MORE = 0;

    private LayoutInflater mLayoutInflater;
    private RefreshCallback mRefreshCallback;
    private MoreCallback mMoreCallback;

    private PageIndex<TItem> mIndex;
    private int mLastPosition;
    private int mItemCount;
    private boolean mHasMore;

    private int mItemTypeCount;
    private Map<Class<?>, ItemType> mItemTypes;

    private static class ItemType {
        int mType;
        int mResource;

        public ItemType(int pIndex, int pResource) {
            super();
            mType = pIndex;
            mResource = pResource;
        }

        @Override
        public String toString() {
            return "ItemType [mIndex=" + mType + ", mResource=" + mResource + "]";
        }
    }

    public interface PageAdapterItem<TItem> {
        void initialize(TItem pItem);

        void setContent(TItem pItem);
    }

    public PageAdapter(LayoutInflater pLayoutInflater) {
        super();
        mLayoutInflater = pLayoutInflater;
        mRefreshCallback = null;
        mMoreCallback = null;

        mIndex = null;
        mLastPosition = -1;
        mItemCount = 0;
        mHasMore = true;

        mItemTypes = Maps.newHashMap();
        mItemTypeCount = 1; // More item is taken into account.
    }

    public void addItemType(Class<?> pType, int pResource) {
        mItemTypes.put(pType, new ItemType(mItemTypeCount, pResource));
        mItemTypeCount = mItemTypes.size() + 1;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        if ((pPosition >= mItemCount) && (mLastPosition != pPosition) /* && (mTimeline.hasMore()) */) {
            if (mMoreCallback != null) mMoreCallback.onMore();
            mLastPosition = pPosition;
        }

        if (mHasMore && (pPosition == mItemCount)) {
            if (pConvertView == null) {
                pConvertView = mLayoutInflater.inflate(R.layout.item_news_more, pParent, false);
            }
            ((NewsMoreItem) pConvertView).setContent();
        } else {
            Object lItem = getItem(pPosition);
            ItemType lItemType = mItemTypes.get(lItem.getClass());
            int lItemResource = lItemType.mResource;

            if (pConvertView == null) {
                pConvertView = mLayoutInflater.inflate(lItemResource, pParent, false);
                ((PageAdapterItem) pConvertView).initialize(lItem);
            }
            ((PageAdapterItem) pConvertView).setContent(lItem);
        }
        return pConvertView;
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
        if (mHasMore && (pPosition == mItemCount)) {
            return ITEM_MORE;
        } else {
            Object lItem = getItem(pPosition);
            ItemType lItemType = mItemTypes.get(lItem.getClass());
            return lItemType.mType;
        }
    }

    // @Override
    // public boolean isEnabled(int position) {
    // return position % 2 == 0;
    // }

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
