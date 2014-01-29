package com.codexperiments.newsroot.ui.fragment;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.BaseFragment;
import com.codexperiments.newsroot.common.rx.AsyncCommand;
import com.codexperiments.newsroot.common.rx.Command;
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
import com.codexperiments.rx.RxAndroid;
import com.codexperiments.rx.RxClickListener;
import com.codexperiments.rx.RxProperty;
import com.codexperiments.rx.RxUIN;

public class NewsListFragment extends BaseFragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    @Inject TweetRepository mTweetRepository;

    int toto = create();
    protected Timeline mTimeline;
    protected PageIndex<News> mTweets;
    protected TimeRange mTimeRange;

    private ListView mUIList;
    private NewsListFragmentAdapter mUIListAdapter;
    private RxClickListener<NewsTweetItem> mTweetItemEvent;
    private RxProperty<TweetDTO> mTweetsProperty;

    private CompositeSubscription mSubcriptions = Subscriptions.from();
    private AsyncCommand<Void, TweetPageResponse> mFindMoreCommand;
    private AsyncCommand<TimeGap, TweetPageResponse> mFindGapCommand = AsyncCommand.create(mSubcriptions);
    private Command<NewsTweetItem> mSelectCommand2 = Command.create();

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
        bind();
        mUIListAdapter = new NewsListFragmentAdapter(this);
        mUIList.setAdapter(mUIListAdapter);
        return lUIFragment;
    }

    protected NewsMoreItem onCreateMoreItem(NewsMoreItem pMoreItem) {
        if (pMoreItem == null) {
            pMoreItem = new NewsMoreItem(getActivity());
        }
        pMoreItem.setContent();
        return pMoreItem;
    }

    protected NewsTimeGapItem onCreateTimeGapItem(NewsTimeGapItem pTimeGapItem, TimeGap pTimeGap, ViewGroup pParent) {
        if (pTimeGapItem == null) {
            pTimeGapItem = NewsTimeGapItem.create(getActivity(), pParent);
            pTimeGapItem.setOnClickListener(mTweetItemEvent);
        }
        pTimeGapItem.setContent(pTimeGap);
        return pTimeGapItem;
    }

    protected NewsTweetItem onCreateTweetItem(NewsTweetItem pTweetItem, TweetDTO pTweet, ViewGroup pParent) {
        if (pTweetItem == null) {
            pTweetItem = NewsTweetItem.create(getActivity(), pParent);
            pTweetItem.setOnClickListener(mTweetItemEvent);
        }
        pTweetItem.setContent(pTweet);
        return pTweetItem;
    }

    public void onInitializeInstanceState(Bundle pBundle) {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }

    protected void bind() {
        mFindGapCommand = createFindGapCommand();

        mTweetItemEvent = RxClickListener.create(RxUIN.convertToListViewItem(mUIList, NewsTweetItem.class));
        mTweetsProperty = RxProperty.create();

        // react(mTweetsProperty.whenAny(TweetDTO.Selected) //
        // .subscribe(RxUIN.toListViewItem(mUIList,
        // NewsTweetItem.class,
        // new Action2<TweetDTO, NewsTweetItem>() {
        // public void call(TweetDTO pTweet, NewsTweetItem pNewsTweetItem) {
        // pNewsTweetItem.setIsSelected(pTweet);
        // }
        // })));
        // react(mSelectCommand2.subscribe(RxUIN.fromListViewItem(mUIList, TweetDTO.class, new Action2<TweetDTO, NewsTweetItem>()
        // {
        // public void call(TweetDTO pTweet, NewsTweetItem pNewsTweetItem) {
        // pTweet.setSelected(!pTweet.isSelected());
        // mTweetsProperty.notify(pTweet, TweetDTO.Selected);
        // }
        // })));
        react(mTweetItemEvent.onClick().subscribe(mSelectCommand2));
    }

    protected void react(Subscription pSubscription) {
        mSubcriptions.add(pSubscription);
    }

    int create() {
        mSelectCommand2.toString();
        return 0;
    }

    protected void onMore() {
        if (mFindMoreCommand == null) {
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
            mFindMoreCommand.register(new Func1<Void, Observable<TweetPageResponse>>() {
                public Observable<TweetPageResponse> call(Void pVoid) {
                    return mTweetRepository.findTweets(mTimeline, TimeGap.pastTimeGap(mTimeRange), 1, 20);
                }
            }).subscribe(new Action1<TweetPageResponse>() {
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
        mFindMoreCommand.execute();
    }

    private Func1<TweetDTO, TweetDTO> doSetSelected() {
        return new Func1<TweetDTO, TweetDTO>() {
            public TweetDTO call(TweetDTO pTweet) {
                pTweet.setSelected(!pTweet.isSelected());
                return pTweet;
            }
        };
    }

    private Func1<TweetDTO, Boolean> selectedProperty() {
        return new Func1<TweetDTO, Boolean>() {
            public Boolean call(TweetDTO pTweet) {
                return Boolean.valueOf(pTweet.isSelected());
            }
        };
    }

    protected AsyncCommand<TimeGap, TweetPageResponse> createFindGapCommand() {
        // return AsyncCommand.create(null, new Func1<TimeGap, Observable<TweetPageResponse>>() {
        // public Observable<TweetPageResponse> call(final TimeGap pTimeGap) {
        // return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
        // public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
        // AndroidScheduler.threadPoolForIO().schedule(new Action0() {
        // public void call() {
        // try {
        // Thread.sleep(5000);
        // pObserver.onNext(null);
        // pObserver.onCompleted();
        // } catch (InterruptedException e) {
        // }
        // }
        // });
        // return Subscriptions.empty();
        // }
        // });
        // }
        // });
        return null;
    }

    // protected Property<Boolean> isSelectedProperty() {
    // return Property.create(new PropertyAccess<Boolean>() {
    // public Boolean get() {
    // return mTweet.isSelected();
    // }
    //
    // public void set(Boolean pValue) {
    // mTweet.setSelected(pValue);
    // }
    // });
    // }

    // protected Property<Boolean> isSelectedProperty() {
    // return Property.create(new PropertyAccess<Boolean>() {
    // public Boolean get() {
    // return mTweet.isSelected();
    // }
    //
    // public void set(Boolean pValue) {
    // mTweet.setSelected(pValue);
    // }
    // });
    // }
}
