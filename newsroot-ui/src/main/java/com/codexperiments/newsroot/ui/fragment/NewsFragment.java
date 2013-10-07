package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.Page;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TimeGapPage;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.MoreCallback;
import com.codexperiments.robolabor.task.TaskManager;

public class NewsFragment extends Fragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterRepository mTwitterRepository;

    private Timeline mTimeline;
    private PageIndex<News> mTweets;
    // private List<News> mTweets;
    // private boolean mFromCache;
    // private boolean mHasMore; // TODO
    // private boolean mLoadingMore;

    // private NewsAdapter mUIListAdapter;
    private PageAdapter<News> mUIListAdapter;
    private ListView mUIList;
    private ProgressDialog mUIDialog;

    private Observable<Void> mOnMore;

    public static final NewsFragment forUser(String pScreenName) {
        NewsFragment lFragment = new NewsFragment();
        Bundle lArguments = new Bundle();
        lArguments.putString(ARG_SCREEN_NAME, pScreenName);
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(final LayoutInflater pLayoutInflater, ViewGroup pContainer, Bundle pBundle) {
        super.onCreateView(pLayoutInflater, pContainer, pBundle);
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterRepository = BaseApplication.getServiceFrom(getActivity(), TwitterRepository.class);

        mTimeline = mTwitterRepository.findTimeline(getArguments().getString(ARG_SCREEN_NAME));
        mTweets = new PageIndex<News>();
        // mTweets = new ArrayList<News>(); // TODO Get from prefs.
        // mFromCache = true;
        // mHasMore = true;
        // mLoadingMore = false;
        mUIDialog = new ProgressDialog(getActivity());
        mUIDialog.setTitle("Please wait...");
        mUIDialog.setMessage("Retrieving tweets ...");
        mUIDialog.setIndeterminate(true);

        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);

        ReactiveCommand<TimeGap> reactiveCommand = new ReactiveCommand<TimeGap>(null);
        reactiveCommand.registerAsync(new Func1<TimeGap, Observable<TweetPageResponse>>() {
            public Observable<TweetPageResponse> call(TimeGap pTimeGap) {
                return null;
            }
        });
        reactiveCommand.subscribe(new Action1<TimeGap>() {
            public void call(TimeGap pTimeGap) {
                mTwitterRepository.findTweets(mTimeline, pTimeGap, 1, 20);
            }
        });

        // mUIListAdapter = new NewsAdapter(pLayoutInflater, mTimeline);
        mUIListAdapter = new PageAdapter<News>(pLayoutInflater, mTimeline);
        final PublishSubject<Void> onFinished = PublishSubject.create();
        mOnMore = onMoreAction(mUIListAdapter);
        mOnMore.startWith(new Void[] { null }).subscribe(new Action1<Void>() {
            public void call(Void pVoid) {
                // moreTweets();
                // Observable<TweetPageResponse> findTweets =
                mTwitterRepository.findTweets(mTimeline, mTimeline.pastGap(), 1, 20)
                                  .observeOn(AndroidScheduler.threadForUI())
                                  .map(new Func1<TweetPageResponse, TweetPage>() {
                                      public TweetPage call(TweetPageResponse pTweetPageResponse) {
                                          return pTweetPageResponse.tweetPage();
                                      }
                                  })
                                  .subscribe(toList(mUIListAdapter, onFinished));

                // bindToList(findTweets, mUIListAdapter);
            }
        });

        Observable<Boolean> onMo = mOnMore.map(new Func1<Void, Boolean>() {
            public Boolean call(Void pVoid) {
                return Boolean.TRUE;
            }
        });
        Observable<Boolean> onDo = onFinished.map(new Func1<Void, Boolean>() {
            public Boolean call(Void pVoid) {
                return Boolean.FALSE;
            }
        });
        Observable.merge(onMo, onDo).subscribe(toDialogVisibleProp(mUIDialog));
        // showProgress(mUIMore.map(new Func1<Void, Boolean>() {
        // public Boolean call(Void pVoid) {
        // return Boolean.TRUE;
        // }
        // }));
        // mUIListAdapter = new BaseAdapter() {
        // int mLastPosition = 0;
        //
        // @Override
        // public View getView(int pPosition, View pConvertView, ViewGroup pParent) {
        // if ((pPosition == mTweets.size() - 1) && (mLastPosition != pPosition) /* && (mTimeline.hasMore()) */) {
        // moreTweets();
        // mUIMore.onNext(null);
        // mLastPosition = pPosition;
        // }
        //
        // NewsItem lUINewsItem;
        // if (pConvertView == null) {
        // lUINewsItem = (NewsItem) pLayoutInflater.inflate(R.layout.item_news, pParent, false);
        // } else {
        // lUINewsItem = (NewsItem) pConvertView;
        // }
        //
        // lUINewsItem.setContent(mTweets.find(pPosition, 1).get(0));
        // return lUINewsItem;
        // }
        //
        // @Override
        // public long getItemId(int pPosition) {
        // return pPosition;
        // }
        //
        // @Override
        // public Object getItem(int pPosition) {
        // return mTweets.find(pPosition, 1);
        // }
        //
        // @Override
        // public int getCount() {
        // return mTweets.size();
        // }
        // };

        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setAdapter(mUIListAdapter);
        mUIDialog = new ProgressDialog(getActivity());

        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());
        return lUIFragment;
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
        // moreTweets();
    }

    @Override
    public void onStop() {
        super.onStop();
        mUIDialog.dismiss();
        mTaskManager.unmanage(this);
        mEventBus.unregisterListener(this);
    }

    public static class ReactiveCommand<TParam> extends Observable<TParam> {
        private PublishSubject<Boolean> mInflight = PublishSubject.create(); // TODO Lazy
        // ScheduledSubject<Exception> exceptions;
        private PublishSubject<TParam> mCommand = PublishSubject.create();
        private Observable<TParam> mTask;
        private Scheduler mScheduler = AndroidScheduler.threadForUI();

        public ReactiveCommand(Observable<TParam> pTask) {
            super(null);
            mCommand = PublishSubject.create();
            mTask = pTask;
        }

        public void execute(TParam pParam) {
            mInflight.onNext(Boolean.TRUE);
            mCommand.onNext(pParam);
            mInflight.onNext(Boolean.FALSE);
        }

        public Observable<TParam> task() {
            return mTask;
        }

        public <TResult> Observable<TResult> registerAsync(final Func1<TParam, Observable<TResult>> pAsyncCommand) {
            return Observable.merge(mCommand.map(pAsyncCommand)).observeOn(AndroidScheduler.threadForUI());
        }

        @Override
        public Subscription subscribe(Observer<? super TParam> pObserver) {
            return mCommand.subscribe(pObserver);
        }
    }

    public Observable<Void> onMoreAction(PageAdapter<News> pPageAdapter) {
        final PublishSubject<Void> lPublisher = PublishSubject.create();
        pPageAdapter.setMoreCallback(new MoreCallback() {
            public void onMore() {
                lPublisher.onNext(null);
            }
        });
        return lPublisher;
    }

    public void refreshTweets() {
        // mTwitterRepository.findTweets(mTimeline, mTimeline.futureGap(), 5, 20)
        // .observeOn(AndroidScheduler.threadForUI())
        // .subscribe(new Observer<TweetPageResponse>() {
        // public void onNext(TweetPageResponse pTweetPageResponse) {
        // mTimeline.add(pTweetPageResponse.tweetPage());
        // mUIListAdapter.notifyDataSetChanged();
        // }
        //
        // public void onCompleted() {
        // // Nothing to do.
        // mUIDialog.dismiss();
        // }
        //
        // public void onError(Throwable pThrowable) {
        // mUIDialog.dismiss();
        // Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
        // pThrowable.printStackTrace();
        // }
        // });
        // Observable<TweetPageResponse> lTweetPageResponses = updateTimeline(mTimeline, mTimeline.futureGap(), 5);
        // lTweetPageResponses = updateList(lTweetPageResponses, mUIListAdapter);
    }

    public void moreTweets() {
        // if (!mLoadingMore) {
        // mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);
        //
        // mLoadingMore = true;
        // mTwitterRepository.findTweets(mTimeline, mTimeline.pastGap(), 1, 20)
        // .observeOn(AndroidScheduler.threadForUI())
        // .subscribe(new Observer<TweetPageResponse>() {
        // TimeGap lRemainingGap;
        //
        // public void onNext(TweetPageResponse pTweetPageResponse) {
        // lRemainingGap = pTweetPageResponse.remainingGap();
        // mTimeline.add(pTweetPageResponse.tweetPage());
        // mUIListAdapter.notifyDataSetChanged();
        // }
        //
        // public void onCompleted() {
        // if (lRemainingGap != null) {
        // mTimeline.add(lRemainingGap);
        // }
        // mLoadingMore = false;
        // mUIDialog.dismiss();
        // }
        //
        // public void onError(Throwable pThrowable) {
        // mUIDialog.dismiss();
        // Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
        // pThrowable.printStackTrace();
        // }
        // });
        // }
        Observable<TweetPageResponse> findTweets = mTwitterRepository.findTweets(mTimeline, mTimeline.pastGap(), 1, 20)
                                                                     .observeOn(AndroidScheduler.threadForUI());
        bindToList(findTweets, mUIListAdapter);
        // Observable<Boolean> showProgress = Observable.from(Boolean.FALSE).startWith(Boolean.TRUE);
        // showProgress(showProgress);
        // new ListViewBinder(mUIList, mUIListAdapter, new PageIndex<News>()).bind(lFindTweets);
        // lTweetPageResponses = updateTimeline(lTweetPageResponses, mTimeline);
        // lFindTweets = updateList(lFindTweets, mUIListAdapter);
    }

    public Observer<Boolean> toDialogVisibleProp(final Dialog pDialog) {
        return new Observer<Boolean>() {
            public void onNext(Boolean pVisible) {
                if (pVisible == Boolean.TRUE) {
                    pDialog.show();
                } else {
                    pDialog.dismiss();
                }
            }

            public void onCompleted() {
                pDialog.dismiss();
            }

            public void onError(Throwable pThrowable) {
                pDialog.dismiss();
                Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                pThrowable.printStackTrace();
            }
        };
    }

    public void showDialog(final Observable<TweetPageResponse> pTweetResponses) {
        // mLoadingMore = true;
        mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);

        pTweetResponses.subscribe(new Observer<TweetPageResponse>() {
            public void onNext(TweetPageResponse pTweetPageResponse) {
            }

            public void onCompleted() {
                mUIDialog.dismiss();
            }

            public void onError(Throwable pThrowable) {
                mUIDialog.dismiss();
                Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                pThrowable.printStackTrace();
            }
        });
    }

    public <TItem> Observer<Page<? extends TItem>> toList(final PageAdapter<TItem> pAdapter,
                                                          final PublishSubject<Void> pOnFinished)
    {
        return new Observer<Page<? extends TItem>>() {
            public void onNext(Page<? extends TItem> pPage) {
                // mTimeline.delete(pTweetPageResponse.initialGap());
                pAdapter.append(pPage);
                // pAdapter.insert(new TimeGapPage(pTweetPageResponse.remainingGap()));
                // mTimeline.add(pTweetPageResponse.tweetPage());
                // mTimeline.add(pTweetPageResponse.remainingGap());
                pAdapter.notifyDataSetChanged();
            }

            public void onCompleted() {
                pOnFinished.onNext(null);
            }

            public void onError(Throwable pThrowable) {
                pOnFinished.onNext(null);
            }
        };
    }

    public void bindToList(final Observable<TweetPageResponse> pTweetResponses, final BaseAdapter pAdapter) {
        // mLoadingMore = true;
        // mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);

        pTweetResponses.subscribe(new Observer<TweetPageResponse>() {
            public void onNext(TweetPageResponse pTweetPageResponse) {
                // mTimeline.delete(pTweetPageResponse.initialGap());
                mTweets.insert(pTweetPageResponse.tweetPage());
                mTweets.insert(new TimeGapPage(pTweetPageResponse.remainingGap()));
                // mTimeline.add(pTweetPageResponse.tweetPage());
                // mTimeline.add(pTweetPageResponse.remainingGap());
                pAdapter.notifyDataSetChanged();
            }

            public void onCompleted() {
                // mLoadingMore = false;
                // mUIDialog.dismiss();
            }

            public void onError(Throwable pThrowable) {
                // mUIDialog.dismiss();
                // Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                pThrowable.printStackTrace();
            }
        });
    }

    // public static abstract class ListViewBinder extends BaseAdapter {
    // private ListView mListView;
    // private BaseAdapter mAdapter;
    // private PageIndex<News> mIndex;
    //
    // public ListViewBinder(ListView pListView, BaseAdapter pAdapter, PageIndex<News> pIndex) {
    // super();
    // mListView = pListView;
    // mAdapter = pAdapter;
    // mIndex = pIndex;
    // }
    //
    // private void bind(final Observable<TweetPageResponse> pTweetResponses) {
    // pTweetResponses.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<TweetPageResponse>() {
    // public void onNext(TweetPageResponse pTweetPageResponse) {
    // // mTimeline.delete(pTweetPageResponse.initialGap());
    // mIndex.insert(pTweetPageResponse.tweetPage());
    // mIndex.insert(new TimeGapPage(pTweetPageResponse.remainingGap()));
    // mAdapter.notifyDataSetChanged();
    // }
    //
    // public void onCompleted() {
    // }
    //
    // public void onError(Throwable pThrowable) {
    // }
    // });
    // }
    //
    // @Override
    // public int getCount() {
    // return mIndex.size();
    // }
    //
    // @Override
    // public Object getItem(int pPosition) {
    // return mIndex.find(pPosition, 1);
    // }
    // }

    // public Observable<TweetPageResponse> updateList(final Observable<TweetPageResponse> pTweetResponses,
    // final BaseAdapter pAdapter)
    // {
    // // mLoadingMore = true;
    // mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);
    //
    // return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
    // public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
    // return pTweetResponses.subscribe(new Observer<TweetPageResponse>() {
    // public void onNext(TweetPageResponse pTweetPageResponse) {
    // pAdapter.notifyDataSetChanged();
    // pObserver.onNext(pTweetPageResponse);
    // }
    //
    // public void onCompleted() {
    // // mLoadingMore = false;
    // mUIDialog.dismiss();
    // pObserver.onCompleted();
    // }
    //
    // public void onError(Throwable pThrowable) {
    // mUIDialog.dismiss();
    // Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
    // pThrowable.printStackTrace();
    // pObserver.onError(pThrowable);
    // }
    // });
    // }
    // });
    // }

    // public Observable<TweetPageResponse> updateTimeline(final Observable<TweetPageResponse> pTweetResponses,
    // final Timeline pTimeline)
    // {
    // return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
    // public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
    // return pTweetResponses.observeOn(AndroidScheduler.threadForUI()).subscribe(new Observer<TweetPageResponse>() {
    // public void onNext(TweetPageResponse pTweetPageResponse) {
    // // mTimeline.delete(pTweetPageResponse.initialGap());
    // pTimeline.add(pTweetPageResponse.tweetPage());
    // pTimeline.add(pTweetPageResponse.remainingGap());
    //
    // pObserver.onNext(pTweetPageResponse);
    // }
    //
    // public void onCompleted() {
    // pObserver.onCompleted();
    // }
    //
    // public void onError(Throwable pThrowable) {
    // pObserver.onError(pThrowable);
    // }
    // });
    // }
    // });
    // }
    //
    // public Observable<TweetPageResponse> updateTimeline(final Timeline pTimeline, final TimeGap pTimeGap, final int pPageCount)
    // {
    // return Observable.create(new OnSubscribeFunc<TweetPageResponse>() {
    // public Subscription onSubscribe(final Observer<? super TweetPageResponse> pObserver) {
    // return mTwitterRepository.findTweets(pTimeline, pTimeline.pastGap(), pPageCount, 20)
    // .observeOn(AndroidScheduler.threadForUI())
    // .subscribe(new Observer<TweetPageResponse>() {
    // TimeGap lRemainingGap = pTimeGap;
    //
    // public void onNext(TweetPageResponse pTweetPageResponse) {
    // // mTimeline.delete(lRemainingGap);
    // lRemainingGap = pTweetPageResponse.remainingGap();
    // pTimeline.add(pTweetPageResponse.tweetPage());
    // pTimeline.add(lRemainingGap);
    //
    // pObserver.onNext(pTweetPageResponse);
    // }
    //
    // public void onCompleted() {
    // pObserver.onCompleted();
    // }
    //
    // public void onError(Throwable pThrowable) {
    // pObserver.onError(pThrowable);
    // }
    // });
    // }
    // });
    // }
}
