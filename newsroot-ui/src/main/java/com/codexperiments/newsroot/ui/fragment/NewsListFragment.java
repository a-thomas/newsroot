package com.codexperiments.newsroot.ui.fragment;

import javax.inject.Inject;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
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
import com.codexperiments.newsroot.common.event.EventBus;
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
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.MoreCallback;
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.rx.RxClickListener;
import com.codexperiments.rx.RxListBinder;
import com.codexperiments.rx.RxProperty;
import com.codexperiments.rx.RxUIN;

public class NewsListFragment extends BaseFragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    @Inject TweetRepository mTweetRepository;

    private Timeline mTimeline;
    private PageIndex<News> mTweets;
    private TimeRange mTimeRange;

    private PageAdapter<News> mUIListAdapter;
    private ListView mUIList;
    // private RxOnListClickEvent<Tweet, NewsTweetItem> mOnListClickEvent;
    // private RxListBindListener mListBindListener;
    private RxListBinder mListBinder;
    // private RxListClickListener<NewsTweetItem, Tweet> mTweetItemEvent;
    private RxClickListener<NewsTweetItem> mTweetItemEvent;
    // private RxListProperty<NewsTweetItem, Tweet> mTweetItemProperty;
    // private RxTodoProperty<ListEvent<NewsTweetItem, Tweet>> mTweetItemProperty;
    // private RxProperty<ListEvent<NewsTweetItem, Tweet>> mTweetItemProperty;
    private RxProperty<TweetDTO> mTweetsProperty;

    private CompositeSubscription mSubcriptions = Subscriptions.from();
    // private Property<Boolean> mSelectedProperty;
    // private Command<Integer, Integer> mSelectCommand;
    // private Property<Tweet> mTweetChangedObservable;
    private AsyncCommand<Void, TweetPageResponse> mFindMoreCommand;
    private AsyncCommand<TimeGap, TweetPageResponse> mFindGapCommand;
    // private Command<ListEvent<NewsTweetItem, Tweet>> mSelectCommand = Command.create();
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

        // Services.
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);

        // Domain.
        mTweets = new TreePageIndex<News>();
        mTimeline = mTweetRepository.findTimeline(getArguments().getString(ARG_SCREEN_NAME));
        mTimeRange = null;
        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());

        // UI.
        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);
        mUIListAdapter = createAdapter(pLayoutInflater, mTweets);
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

        bind();
        mUIList.setAdapter(mUIListAdapter);
        return lUIFragment;
    }

    public void onInitializeInstanceState(Bundle pBundle) {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }

    protected PageAdapter<News> createAdapter(final LayoutInflater pLayoutInflater, final PageIndex<News> pIndex) {
        return new PageAdapter<News>(pIndex) {
            private boolean mHasMore = true;

            @Override
            public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
                super.getView(pPosition, pConvertView, pParent);
                Object lItem = null;
                if (isLastItem(pPosition) && mHasMore) {
                    return recycleMoreItem((NewsMoreItem) pConvertView);
                } else {
                    lItem = getItem(pPosition);
                    if (lItem.getClass() == TimeGap.class) {
                        pConvertView = recycleTimeGapItem((NewsTimeGapItem) pConvertView, (TimeGap) lItem, pParent);
                    } else if (lItem.getClass() == TweetDTO.class) {
                        pConvertView = recycleTweetItem((NewsTweetItem) pConvertView, (TweetDTO) lItem, pParent);
                    }
                }
                doRecycly(pPosition, pConvertView, lItem);
                return pConvertView;
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
                if (lItem.getClass() == TweetDTO.class) return 1;
                else if (lItem.getClass() == TimeGap.class) return 2;
                else throw new IllegalStateException();
            }
        };
    }

    protected NewsMoreItem recycleMoreItem(NewsMoreItem pMoreItem) {
        if (pMoreItem == null) {
            pMoreItem = new NewsMoreItem(getActivity());
            // pMoreItem.setOnClickListener(mClickListener);
        }
        pMoreItem.setContent();
        return pMoreItem;
    }

    protected NewsTimeGapItem recycleTimeGapItem(NewsTimeGapItem pTimeGapItem, TimeGap pTimeGap, ViewGroup pParent) {
        if (pTimeGapItem == null) {
            pTimeGapItem = NewsTimeGapItem.create(getActivity(), pParent);
            pTimeGapItem.setOnClickListener(mTweetItemEvent);
        }
        pTimeGapItem.setContent(pTimeGap);
        return pTimeGapItem;
    }

    protected NewsTweetItem recycleTweetItem(NewsTweetItem pTweetItem, TweetDTO pTweet, ViewGroup pParent) {
        if (pTweetItem == null) {
            pTweetItem = NewsTweetItem.create(getActivity(), pParent);
            pTweetItem.setOnClickListener(mTweetItemEvent);
        }
        pTweetItem.setContent(pTweet);
        return pTweetItem;
    }

    protected void bind() {
        mFindMoreCommand = createFindMoreCommand();
        mFindGapCommand = createFindGapCommand();

        react(mFindMoreCommand.subscribe(new Action1<TweetPageResponse>() {
            public void call(TweetPageResponse pTweetPageResponse) {
                onMoreData(pTweetPageResponse);
            }
        }));
        mUIListAdapter.setMoreCallback(new MoreCallback() {
            public void onMore() {
                mFindMoreCommand.execute();
            }
        });

        mListBinder = RxListBinder.create(mUIList);
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

        mUIListAdapter.setRecycleCallback(mListBinder);
    }

    protected void react(Subscription pSubscription) {
        mSubcriptions.add(pSubscription);
    }

    protected void onMoreData(TweetPageResponse pTweetPageResponse) {
        TweetPage lTweetPage = pTweetPageResponse.tweetPage();
        mTimeRange = TimeRange.append(mTimeRange, lTweetPage.tweets());
        // mTweets.insert(new NewsPage(pTweetPageResponse.initialGap()));
        mTweets.insert(new NewsPage(lTweetPage));

        TimeGap lTimeGap = pTweetPageResponse.remainingGap();
        mTweets.insert(new NewsPage(lTimeGap));
        mUIListAdapter.notifyDataSetChanged();
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

    protected AsyncCommand<Void, TweetPageResponse> createFindMoreCommand() {
        return AsyncCommand.create(new Func1<Void, Observable<TweetPageResponse>>() {
            public Observable<TweetPageResponse> call(Void pVoid) {
                return mTweetRepository.findTweets(mTimeline, TimeGap.pastTimeGap(mTimeRange), 1, 20);
            }
        });
    }

    protected AsyncCommand<TimeGap, TweetPageResponse> createFindGapCommand() {
        return AsyncCommand.create(new Func1<TimeGap, Observable<TweetPageResponse>>() {
            public Observable<TweetPageResponse> call(final TimeGap pTimeGap) {
                return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
                    public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
                        AndroidScheduler.threadPoolForIO().schedule(new Action0() {
                            public void call() {
                                try {
                                    Thread.sleep(5000);
                                    pObserver.onNext(null);
                                    pObserver.onCompleted();
                                } catch (InterruptedException e) {
                                }
                            }
                        });
                        return Subscriptions.empty();
                    }
                });
            }
        });
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
