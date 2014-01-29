package com.codexperiments.newsroot.ui.fragment;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.News;
import com.codexperiments.newsroot.domain.tweet.TimeGap;

public class NewsListFragmentAdapter extends BaseAdapter {
    private Activity mActivity;
    private NewsListFragment mListFragment;
    private PageIndex<News> mIndex;
    private int mIndexSize;

    private int mLastItemSeen = -1;
    private boolean mHasMore = true;

    public NewsListFragmentAdapter(NewsListFragment pListFragment) {
        super();
        mActivity = pListFragment.getActivity();
        mListFragment = pListFragment;
        mIndex = mListFragment.mTweets;
        mIndexSize = mIndex.size();
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
        return mIndex.find(mIndexSize - pPosition - 1, 1).get(0);
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
        if (isLastItem(pPosition) && (pPosition > mLastItemSeen)) {
            mListFragment.onMore();
            mLastItemSeen = pPosition;
        }
        Object lItem = null;
        if (isLastItem(pPosition) && mHasMore) {
            return mListFragment.onCreateMoreItem((NewsMoreItem) pConvertView);
        } else {
            lItem = getItem(pPosition);
            if (lItem.getClass() == TimeGap.class) {
                pConvertView = mListFragment.onCreateTimeGapItem((NewsTimeGapItem) pConvertView, (TimeGap) lItem, pParent);
            } else if (lItem.getClass() == TweetDTO.class) {
                pConvertView = mListFragment.onCreateTweetItem((NewsTweetItem) pConvertView, (TweetDTO) lItem, pParent);
            }
        }
        return pConvertView;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }
}
