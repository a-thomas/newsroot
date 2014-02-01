package com.codexperiments.newsroot.ui.fragment;

import javax.inject.Inject;

import rx.util.functions.Action1;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.BaseFragment;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.common.structure.TreePageIndex;
import com.codexperiments.newsroot.data.tweet.TweetDTO;
import com.codexperiments.newsroot.domain.tweet.News;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.TimeRange;
import com.codexperiments.newsroot.domain.tweet.Timeline;
import com.codexperiments.newsroot.domain.tweet.TweetPage;
import com.codexperiments.newsroot.repository.tweet.TweetPageResponse;
import com.codexperiments.newsroot.repository.tweet.TweetRepository;
import com.codexperiments.newsroot.ui.fragment.NewsListFragmentAdapter.ItemFactory;
import com.codexperiments.rx.RxAndroid;

public class NewsListFragment extends BaseFragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    @Inject TweetRepository mTweetRepository;

    protected Timeline mTimeline;
    protected PageIndex<News> mTweets;
    protected TimeRange mTimeRange;

    private ListView mUIList;
    private NewsListFragmentAdapter mUIListAdapter;

    public static final NewsListFragment forUser(String pScreenName) {
        NewsListFragment lFragment = new NewsListFragment();
        Bundle lArguments = new Bundle();
        lArguments.putString(ARG_SCREEN_NAME, pScreenName);
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater pLayoutInflater, ViewGroup pContainer, Bundle pBundle) {
        super.onCreateView(pLayoutInflater, pContainer, pBundle);
        BaseApplication.from(getActivity()).dependencies().inject(this);

        // Domain.
        mTweets = new TreePageIndex<News>();
        mTimeline = mTweetRepository.findTimeline(getArguments().getString(ARG_SCREEN_NAME));
        mTimeRange = null;
        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());

        // UI.
        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

        // Binding.
        mUIListAdapter = new NewsListFragmentAdapter(this);
        mUIList.setAdapter(mUIListAdapter);
        return lUIFragment;
    }

    public void onInitializeInstanceState(Bundle pBundle) {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }

    protected ItemFactory<NewsMoreItem> onCreateMoreItem() {
        return new ItemFactory<NewsMoreItem>() {
            public NewsMoreItem onCreateItem(int pPosition, ViewGroup pParent) {
                NewsMoreItem lMoreItemView = new NewsMoreItem(getActivity());
                lMoreItemView.setOnClickListener(mOnClickListener);
                return lMoreItemView;
            }

            OnClickListener mOnClickListener = new OnClickListener() {
                public void onClick(View pView) {
                    onFindMore();
                }
            };
        };
    }

    protected ItemFactory<NewsTweetItem> onCreateTweetItem() {
        return new ItemFactory<NewsTweetItem>() {
            public NewsTweetItem onCreateItem(int pPosition, ViewGroup pParent) {
                NewsTweetItem lTweetItem = NewsTweetItem.create(getActivity(), pParent);
                lTweetItem.setOnClickListener(mOnClickListener);
                return lTweetItem;
            }

            OnClickListener mOnClickListener = new OnClickListener() {
                public void onClick(View pView) {
                    NewsTweetItem lTweetItem = mUIListAdapter.findItem(mUIList, pView, NewsTweetItem.class);
                    onSelectTweet(lTweetItem);
                }
            };
        };
    }

    protected ItemFactory<NewsTimeGapItem> onCreateTimeGapItem() {
        return new ItemFactory<NewsTimeGapItem>() {
            public NewsTimeGapItem onCreateItem(int pPosition, ViewGroup pParent) {
                NewsTimeGapItem lTimeGapItem = NewsTimeGapItem.create(getActivity(), pParent);
                lTimeGapItem.setOnClickListener(mOnClickListener);
                return lTimeGapItem;
            }

            OnClickListener mOnClickListener = new OnClickListener() {
                public void onClick(View pView) {
                    int lPosition = mUIList.getPositionForView(pView);
                    if (lPosition != AdapterView.INVALID_POSITION) {
                        onFindGap(mUIListAdapter.toIndex(lPosition));
                    }
                }
            };

        };
    }

    protected void onBindMoreItem(NewsMoreItem pMoreItem) {
        pMoreItem.enable();
    }

    protected void onBindTweetItem(final NewsTweetItem pTweetItem, int pIndex) {
        TweetDTO lTweet = (TweetDTO) mTweets.find(pIndex, 1).get(0);
        pTweetItem.setContent(lTweet);
    }

    protected void onBindTimeGapItem(final NewsTimeGapItem pTimeGapItem, int pIndex) {
        TimeGap lTimeGap = (TimeGap) mTweets.find(pIndex, 1).get(0);
        pTimeGapItem.setContent(lTimeGap);
    }

    protected void onSelectTweet(NewsTweetItem lTweetItem) {
        lTweetItem.toggleSelection();
    }

    protected void onFindMore() {
        RxAndroid.from(mTweetRepository.findTweets(mTimeline, TimeGap.pastTimeGap(mTimeRange), 1, 20), this)
                 .subscribe(new Action1<TweetPageResponse>() {
                     public void call(TweetPageResponse pTweetPageResponse) {
                         TweetPage lTweetPage = pTweetPageResponse.tweetPage();
                         mTimeRange = TimeRange.append(mTimeRange, lTweetPage.tweets());
                         // mTweets.insert(new NewsPage(pTweetPageResponse.initialGap()));
                         mTweets.insert(new NewsPage(lTweetPage));

                         TimeGap lTimeGap = pTweetPageResponse.initialGap();
                         mTweets.insert(new NewsPage(lTimeGap));
                         mUIListAdapter.notifyDataSetChanged();
                     }
                 });
    }

    protected void onFindGap(int pIndex) {
        TimeGap pTimeGap = (TimeGap) mTweets.find(pIndex, 1).get(0);
        RxAndroid.from(mTweetRepository.findTweets(mTimeline, pTimeGap, 1, 20), this) //
                 .subscribe(new Action1<TweetPageResponse>() {
                     public void call(TweetPageResponse pTweetPageResponse) {
                         mUIListAdapter.notifyDataSetChanged();
                     }
                 });
    }
}
