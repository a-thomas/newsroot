package com.codexperiments.newsroot.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.domain.twitter.News;

public class NewsAdapter extends BaseAdapter {
    private static final int DEFAULT_LIST_SIZE = 100;

    private LayoutInflater mLayoutInflater;
    private Callback mCallback;

    // private Observable<Observable<News>> mNewsObservable;
    private List<News> mTweets;
    private boolean mHasMore;
    private int mLastPosition;

    public NewsAdapter(LayoutInflater pLayoutInflater,
                       Observable<Observable<News>> pMoreNewsObservable,
                       boolean pHasMore,
                       Callback pCallback)
    {
        super();
        mLayoutInflater = pLayoutInflater;
        mCallback = pCallback;

        mTweets = new ArrayList<News>(DEFAULT_LIST_SIZE);
        mHasMore = pHasMore;
        mLastPosition = 0;

        subscribeMore(pMoreNewsObservable);
    }

    protected void subscribeMore(Observable<Observable<News>> pMoreNewsObservable) {
        pMoreNewsObservable.subscribe(new Observer<Observable<News>>() {
            public void onNext(Observable<News> pNews) {
                pNews.subscribe(new Observer<News>() {
                    public void onNext(News pNews) {
                        mTweets.add(pNews);
                    }

                    public void onCompleted() {
                        notifyDataSetChanged();
                    }

                    public void onError(Throwable pThrowable) {
                        mCallback.onLoadMoreError(pThrowable);
                    }
                });
            }

            public void onCompleted() {
            }

            public void onError(Throwable pThrowable) {
                mCallback.onLoadMoreError(pThrowable);
            }
        });
    }

    @Override
    public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
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
    public long getItemId(int pPosition) {
        return pPosition;
    }

    @Override
    public Object getItem(int pPosition) {
        return mTweets.get(pPosition);
    }

    @Override
    public int getCount() {
        return mTweets.size();
    }

    public void notifyDataSetChanged(List<News> pTweets) {
        mTweets = pTweets;
        super.notifyDataSetChanged();
    }

    public interface Callback {
        void onLoadMore();

        void onLoadMoreError(Throwable pThrowable);
    }
}
