package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.common.rx.AsyncCommand;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.common.structure.RxPageIndex;
import com.codexperiments.newsroot.common.structure.TreePageIndex;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TimeRange;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.robolabor.task.TaskManager;

public class NewsListFragment extends Fragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterRepository mTwitterRepository;

    private Timeline mTimeline;
    private RxPageIndex<News> mTweets;
    private TimeRange mTimeRange;

    private PageAdapter<News> mUIListAdapter;
    private ListView mUIList;
    private CompositeSubscription mSubcriptions;
    private AsyncCommand<Void, TweetPageResponse> mFindMoreCommand;

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
        // Services.
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterRepository = BaseApplication.getServiceFrom(getActivity(), TwitterRepository.class);

        // Domain.
        PageIndex<News> lIndex = new TreePageIndex<News>();
        mTimeline = mTwitterRepository.findTimeline(getArguments().getString(ARG_SCREEN_NAME));
        mTweets = RxPageIndex.newPageIndex();
        mTimeRange = null;
        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());

        // UI.
        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);
        mUIListAdapter = createAdapter(pLayoutInflater, lIndex);
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

        bind();
        mUIList.setAdapter(mUIListAdapter);
        return lUIFragment;
    }

    protected PageAdapter<News> createAdapter(final LayoutInflater pLayoutInflater, final PageIndex<News> pIndex) {
        return new PageAdapter<News>(pIndex) {
            private boolean mHasMore = true;

            @Override
            public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
                if (isLastItem(pPosition) && mHasMore) {
                    return createMoreItem((NewsMoreItem) pConvertView);
                } else {
                    Object lItem = getItem(pPosition);
                    if (lItem.getClass() == TimeGap.class) {
                        return createTimeGapItem((NewsTimeGapItem) pConvertView, (TimeGap) lItem);
                    } else if (lItem.getClass() == Tweet.class) {
                        return createTweetItem((NewsTweetItem) pConvertView, (Tweet) lItem);
                    }
                }
                throw new IllegalStateException();
            }

            @Override
            public int getCount() {
                return (mHasMore) ? super.getCount() + 1 : getCount();
            }

            @Override
            public long getItemId(int pPosition) {
                if (isLastItem(pPosition) && mHasMore) return -1;
                else return pPosition;
            }

            @Override
            public int getViewTypeCount() {
                return 3;
            }

            @Override
            public int getItemViewType(int pPosition) {
                if (isLastItem(pPosition) && mHasMore) return 0;

                Object lItem = getItem(pPosition);
                if (lItem.getClass() == NewsTimeGapItem.class) return 1;
                else if (lItem.getClass() == NewsTimeGapItem.class) return 2;
                else throw new IllegalStateException();
            }
        };
    }

    protected NewsMoreItem createMoreItem(NewsMoreItem pMoreItem) {
        if (pMoreItem == null) {
            pMoreItem = new NewsMoreItem(getActivity());
        }
        pMoreItem.setContent();
        return pMoreItem;
    }

    protected NewsTimeGapItem createTimeGapItem(NewsTimeGapItem pTimeGapItem, TimeGap pTimeGap) {
        if (pTimeGapItem == null) {
            pTimeGapItem = new NewsTimeGapItem(getActivity());
        }
        pTimeGapItem.setContent(pTimeGap);
        return pTimeGapItem;
    }

    protected NewsTweetItem createTweetItem(NewsTweetItem pTweetItem, Tweet pTweet) {
        if (pTweetItem == null) {
            pTweetItem = new NewsTweetItem(getActivity());
        }
        pTweetItem.setContent(pTweet);
        return pTweetItem;
    }

    protected void bind() {
        mSubcriptions = Subscriptions.create();
        mFindMoreCommand = AsyncCommand.create(new Func1<Void, Observable<TweetPageResponse>>() {
            public Observable<TweetPageResponse> call(Void pVoid) {
                return mTwitterRepository.findTweets(mTimeline, TimeGap.pastTimeGap(mTimeRange), 1, 20);
            }
        });
        mSubcriptions.add(mFindMoreCommand.subscribe(new Action1<TweetPageResponse>() {
            public void call(TweetPageResponse pTweetPageResponse) {
                TweetPage lPage = pTweetPageResponse.tweetPage();
                mTimeRange = TimeRange.append(mTimeRange, lPage.tweets());
                mTweets.insert(new NewsPage(lPage));
                // XXX
                if (lPage.size() > 15) {
                    long id = lPage.tweets().get(15).getId() - 1;
                    mTweets.insert(new NewsPage(new TimeGap(id, id - 1)));
                }

            }
        }));

        mSubcriptions.add(RxUI.fromOnMoreAction(mUIListAdapter).subscribe(mFindMoreCommand));
        mSubcriptions.add(mTweets.onInsert().subscribe(RxUI.toListView(mUIListAdapter)));
    }

    public void onInitializeInstanceState(Bundle pBundle) {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventBus.registerListener(this);
        mTaskManager.manage(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mTaskManager.unmanage(this);
        mEventBus.unregisterListener(this);
    }
}
