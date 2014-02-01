package com.codexperiments.newsroot.ui.fragment.newslist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.News;
import com.codexperiments.newsroot.domain.tweet.TimeGap;

public class NewsListFragmentAdapter extends BaseAdapter {
    private NewsListFragment mListFragment;
    private PageIndex<News> mIndex;
    private int mIndexSize;

    private ItemFactory<NewsMoreItem> mMoreItemFactory;
    private ItemFactory<NewsTweetItem> mTweetItemFactory;
    private ItemFactory<NewsTimeGapItem> mTimeGapItemFactory;

    private int mLastItemSeen = -1;
    private boolean mHasMore = true;

    public NewsListFragmentAdapter(NewsListFragment pListFragment) {
        super();
        mListFragment = pListFragment;
        mIndex = mListFragment.mTweets;
        mIndexSize = mIndex.size();

        mMoreItemFactory = mListFragment.onCreateMoreItem();
        mTweetItemFactory = mListFragment.onCreateTweetItem();
        mTimeGapItemFactory = mListFragment.onCreateTimeGapItem();
    }

    public boolean isLastItem(int pPosition) {
        return (pPosition == getCount() - 1);
    }

    @Override
    public void notifyDataSetChanged() {
        mIndex = mListFragment.mTweets;
        mIndexSize = mIndex.size();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mHasMore) ? mIndexSize + 1 : mIndexSize;
    }

    @Override
    public Object getItem(int pPosition) {
        return mIndex.find(toIndex(pPosition), 1).get(0);
    }

    public int toIndex(int pPosition) {
        return mIndexSize - pPosition - 1;
    }

    @Override
    public long getItemId(int pPosition) {
        if (isLastItem(pPosition) && mHasMore) return -1;
        else return pPosition;
    }

    @Override
    public int getItemViewType(int pPosition) {
        if (isLastItem(pPosition) && mHasMore) return 0;

        Object lItem = getItem(pPosition);
        if (lItem.getClass() == TweetDTO.class) return 1;
        else if (lItem.getClass() == TimeGap.class) return 2;
        else throw new IllegalStateException();
    }

    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        Object lItem = null;
        // More item.
        if (isLastItem(pPosition) && mHasMore) {
            NewsMoreItem lMoreItem = (pConvertView != null) ? (NewsMoreItem) pConvertView
                            : mMoreItemFactory.onCreateItem(pPosition, pParent);
            mListFragment.onBindMoreItem(lMoreItem);

            // Notifies more items are requested the first time we see the More item
            if (pPosition > mLastItemSeen) {
                lMoreItem.performClick();
                mLastItemSeen = pPosition;
            }
            return lMoreItem;
        } else {
            lItem = getItem(pPosition);
            int lIndex = toIndex(pPosition);
            // TimeGap item.
            if (lItem.getClass() == TimeGap.class) {
                if (pConvertView == null) pConvertView = mTimeGapItemFactory.onCreateItem(pPosition, pParent);
                mListFragment.onBindTimeGapItem((NewsTimeGapItem) pConvertView, lIndex);
            }
            // Tweet Item.
            else if (lItem.getClass() == TweetDTO.class) {
                if (pConvertView == null) pConvertView = mTweetItemFactory.onCreateItem(pPosition, pParent);
                mListFragment.onBindTweetItem((NewsTweetItem) pConvertView, lIndex);
            }
            return pConvertView;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    public <TItemView> TItemView findItem(ListView pListView, View pView, Class<TItemView> pItemViewClass) {
        // If there is a ClassCastException, that means that the view wasn't an item from the list.
        View lListItem = pView;
        View lView = (View) pView.getParent();
        while (lView != pListView) {
            lListItem = lView;
            lView = (View) lListItem.getParent();
        }

        if (pItemViewClass.isInstance(lListItem)) {
            return pItemViewClass.cast(lListItem);
        } else {
            return null;
        }
    }

    public interface ItemFactory<TView> {
        TView onCreateItem(int pPosition, ViewGroup pParent);
    }
}
