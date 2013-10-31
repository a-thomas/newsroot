package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import rx.Observable.OnSubscribeFunc;
import rx.Observer;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Action2;
import rx.util.functions.Func1;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.common.rx.AsyncCommand;
import com.codexperiments.newsroot.common.rx.ListClickEvent;
import com.codexperiments.newsroot.common.rx.RxListClickListener;
import com.codexperiments.newsroot.common.rx.RxListProperty;
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
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.RxRecycleCallback;
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
    // private RxOnListClickEvent<Tweet, NewsTweetItem> mOnListClickEvent;
    private RxListClickListener<NewsTweetItem, Tweet> mTweetItemEvent;
    private RxListProperty<NewsTweetItem, Tweet> mTweetItemProperty;

    private CompositeSubscription mSubcriptions;
    // private Property<Boolean> mSelectedProperty;
    // private Command<Integer, Integer> mSelectCommand;
    // private Property<Tweet> mTweetChangedObservable;
    private AsyncCommand<Void, TweetPageResponse> mFindMoreCommand;
    private AsyncCommand<TimeGap, TweetPageResponse> mFindGapCommand;

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
        mTweets = RxPageIndex.newPageIndex(lIndex);
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
                    } else if (lItem.getClass() == Tweet.class) {
                        pConvertView = recycleTweetItem((NewsTweetItem) pConvertView, (Tweet) lItem, pParent);
                    }
                }
                doRecycly((News) lItem, pConvertView);
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
                if (lItem.getClass() == Tweet.class) return 1;
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

    protected NewsTweetItem recycleTweetItem(NewsTweetItem pTweetItem, Tweet pTweet, ViewGroup pParent) {
        if (pTweetItem == null) {
            pTweetItem = NewsTweetItem.create(getActivity(), pParent);
            pTweetItem.setOnClickListener(mTweetItemEvent);
        }
        pTweetItem.setContent(pTweet);
        return pTweetItem;
    }

    protected void bind() {
        mSubcriptions = Subscriptions.create();
        mFindMoreCommand = createFindMoreCommand();
        mFindGapCommand = createFindGapCommand();
        // mTweetChangedObservable = Property.create(null);

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
                Log.e("aaaaaaaa", "sfsdfqsdfsdfq");
            }
        }));
        mSubcriptions.add(RxUI.fromOnMoreAction(mUIListAdapter).subscribe(mFindMoreCommand));
        mSubcriptions.add(mTweets.onInsert().subscribe(RxUI.toListView(mUIListAdapter)));

        // mSelectCommand = Command.create();
        // mSubcriptions.add(mItemClickEvent.subscribe(mSelectCommand));
        // mSubcriptions.add(mSelectCommand.subscribe(Property.toggle(mSelectedProperty)));
        RxRecycleCallback<News> lRecycling = RxUI.fromRecycleListItem(News.class);
        mUIListAdapter.setRecycleCallback(lRecycling);
        Observable<NewsTweetItem> lSelectedViewsRecycle = lRecycling.toViews().ofType(NewsTweetItem.class);
        Observable<Boolean> recycleSelected = lRecycling.toItems().ofType(Tweet.class).map(new Func1<Tweet, Boolean>() {
            public Boolean call(Tweet pTweet) {
                return Boolean.valueOf(pTweet.isSelected());
            }
        });
        // .subscribe(RxUI.toActivated(lSelectedViewsRecycle.ofType(NewsTweetItem.class)));

        mTweetItemEvent = RxListClickListener.create(mUIList, NewsTweetItem.class, Tweet.class);
        mTweetItemProperty = RxListProperty.create(mUIList, NewsTweetItem.class, Tweet.class);
        mSubcriptions.add(mTweetItemEvent.onClick().subscribe(new Action1<ListClickEvent<NewsTweetItem, Tweet>>() {
            public void call(ListClickEvent<NewsTweetItem, Tweet> pEvent) {
                Tweet pTweet = pEvent.getItem();
                pTweet.setSelected(!pTweet.isSelected());
                mTweetItemProperty.onNext(pTweet);
            }
        }));
        mTweetItemProperty.register(new Action2<NewsTweetItem, Tweet>() {
            public void call(NewsTweetItem pNewsTweetItem, Tweet pTweet) {
                pNewsTweetItem.setSelected(pTweet.isSelected());
            }
        });
        recycleSelected.subscribe(RxUI.toActivated(lSelectedViewsRecycle));
        // map(new Func1<ListClickEvent<NewsTweetItem, Tweet>, Boolean>() {
        // public Boolean call(ListClickEvent<NewsTweetItem, Tweet> pEvent) {
        // Tweet pTweet = pEvent.getItem();
        // pTweet.setSelected(!pTweet.isSelected());
        // return Boolean.valueOf(pTweet.isSelected());
        // }
        // });
        // Observable<View> lViews = mOnListClickEvent.views().map(new Func1<NewsTweetItem, View>() {
        // public View call(NewsTweetItem pNewsTweetItem) {
        // return (View) pNewsTweetItem.findViewById(R.id.item_news_name).getParent();
        // }
        // });
        // Observable.merge(recycleSelected, lSelected).subscribe(RxUI.toActivated(Observable.merge(lViews,
        // lSelectedViewsRecycle)));
        // lSelectedNews.ofType(TimeGap.class).subscribe(mFindGapCommand);

        // RxUI.toListActivated2(lSelected, lViews);
        // Observable.combineLatest(lSelected, lViews, new Func2<Boolean, View, Boolean>() {
        // public Boolean call(Boolean pValue, View pView) {
        // pView.setActivated(pValue);
        // return pValue;
        // }
        // }).subscribe(new Action1<Object>() {
        // public void call(Object pT1) {
        // }
        // });

        // Observable<NewsTweetItem> lTweetSelected = lSelectedNews.ofType(Tweet.class)
        // .map(doSetSelected())
        // .map(RxUI.toListItem(mUIList, NewsTweetItem.class))
        // .map(selectedProperty())
        // .subscribe(RxUI.toActivated(this));
        // ListProperty<Tweet, View> lTweetItemProperty;
        // lTweetItemProperty = ListProperty.create(mUIList, /*
        // * new Func1<NewsTweetItem, View>() { public View call(NewsTweetItem
        // * pView) { return pView; } }
        // */RxUI.self(), new Action2<Tweet, View>() {
        // public void call(Tweet pTweet, View pInnerView) {
        // pInnerView.setActivated(pTweet.isSelected());
        // }
        // });
        // Observer<Boolean> lActivated = RxUI.toActivated(mUIList);
        // lSelectedNews.ofType(Tweet.class).map(doSetSelected()).subscribe(lTweetItemProperty);

        // lSelectedNews.ofType(Tweet.class).subscribe(new Action1<Tweet>() {
        // public void call(Tweet pTweet) {
        // pTweet.setSelected(!pTweet.isSelected());
        // mTweets.update(pTweet);
        // // mSelectedProperty.onNext(pTweet.isSelected());
        // }
        // });
        // mSubcriptions.add(mFindGapCommand.isRunning().subscribe(RxUI.toActivated(this)));
        // mSubcriptions.add(mFindGapCommand.isRunning().subscribe(RxUI.toDisabled(this)));

        // mSelectedProperty = isSelectedProperty();
        // mSubcriptions.add(RxUI.fromClick(mUIList).subscribe(Property.toggle(mSelectedProperty)));
        // mSubcriptions.add(mSelectedProperty.subscribe(RxUI.toActivated(this)));
    }

    private Func1<Tweet, Tweet> doSetSelected() {
        return new Func1<Tweet, Tweet>() {
            public Tweet call(Tweet pTweet) {
                pTweet.setSelected(!pTweet.isSelected());
                return pTweet;
            }
        };
    }

    private Func1<Tweet, Boolean> selectedProperty() {
        return new Func1<Tweet, Boolean>() {
            public Boolean call(Tweet pTweet) {
                return Boolean.valueOf(pTweet.isSelected());
            }
        };
    }

    protected AsyncCommand<Void, TweetPageResponse> createFindMoreCommand() {
        return AsyncCommand.create(new Func1<Void, Observable<TweetPageResponse>>() {
            public Observable<TweetPageResponse> call(Void pVoid) {
                return mTwitterRepository.findTweets(mTimeline, TimeGap.pastTimeGap(mTimeRange), 1, 20);
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
