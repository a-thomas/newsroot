package com.codexperiments.newsroot.ui.fragment;

import java.util.List;

import twitter4j.Status;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.codexperiments.newsroot.R;

public class NewsAdapter extends BaseAdapter
{
    private Callback mCallback;
    private LayoutInflater mLayoutInflater;

    private List<Status> mTweets;
    private boolean mHasMore;
    private int mLastPosition = 0;

    public NewsAdapter(LayoutInflater pLayoutInflater, List<Status> pTweets, boolean pHasMore, Callback pCallback)
    {
        super();
        mCallback = pCallback;
        mLayoutInflater = pLayoutInflater;
        mTweets = pTweets;
        mHasMore = pHasMore;
    }

    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent)
    {
        if ((pPosition == mTweets.size() - 1) && (mLastPosition != pPosition) && (mHasMore)) {
            mCallback.onLoadMore();
            mLastPosition = pPosition;
        }

        NewsItem lUINewsItem;
        if (pConvertView == null) {
            lUINewsItem = (NewsItem) mLayoutInflater.inflate(R.layout.item_news, pParent, false);
        } else {
            lUINewsItem = (NewsItem) pConvertView;
        }

        lUINewsItem.setContent(mTweets.get(pPosition));
        return lUINewsItem;
    }

    @Override
    public long getItemId(int pPosition)
    {
        return mTweets.get(pPosition).getId();
    }

    @Override
    public Object getItem(int pPosition)
    {
        return mTweets.get(pPosition);
    }

    @Override
    public int getCount()
    {
        return mTweets.size();
    }

    public void notifyDataSetChanged(List<Status> pTweets)
    {
        mTweets = pTweets;
        super.notifyDataSetChanged();
    }


    public interface Callback
    {
        void onLoadMore();
    }
}
