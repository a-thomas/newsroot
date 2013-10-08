package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.newsroot.ui.fragment.NewsPresenter.NewsView;
import com.codexperiments.robolabor.task.TaskManager;

public class NewsFragment extends Fragment implements NewsView {
    private static final String ARG_SCREEN_NAME = "screenName";

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterRepository mTwitterRepository;

    private NewsPresenter mPresenter;

    // private Timeline mTimeline;
    // private PageIndex<News> mTweets;
    // private List<News> mTweets;
    // private boolean mFromCache;
    // private boolean mHasMore; // TODO
    // private boolean mLoadingMore;

    // private NewsAdapter mUIListAdapter;
    // private PageAdapter<News> mUIListAdapter;
    private PageAdapter<News> mUIListAdapter;
    private ListView mUIList;
    private ProgressDialog mUIDialog;

    // private Observable<Void> mOnMoreAction;

    // private AsyncCommand<Void, TweetPageResponse> mFindMoreCommand;

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

        // mTimeline = mTwitterRepository.findTimeline(getArguments().getString(ARG_SCREEN_NAME));

        // mTweets = new PageIndex<News>();
        // mTweets = new ArrayList<News>(); // TODO Get from prefs.
        // mFromCache = true;
        // mHasMore = true;
        // mLoadingMore = false;
        mUIDialog = new ProgressDialog(getActivity());
        mUIDialog.setTitle("Please wait...");
        mUIDialog.setMessage("Retrieving tweets ...");
        mUIDialog.setIndeterminate(true);

        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);

        // mUIListAdapter = new NewsAdapter(pLayoutInflater, mTimeline);
        mUIListAdapter = new PageAdapter<News>(pLayoutInflater);
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setAdapter(mUIListAdapter);
        mUIDialog = new ProgressDialog(getActivity());

        // Observable<Boolean> onMo = onMoreAction.map(new Func1<Void, Boolean>() {
        // public Boolean call(Void pVoid) {
        // return Boolean.TRUE;
        // }
        // });
        // Observable<Boolean> onDo = onFinished.map(new Func1<Void, Boolean>() {
        // public Boolean call(Void pVoid) {
        // return Boolean.FALSE;
        // }
        // });
        // Observable.merge(onMo, onDo).subscribe(RxUI.toDialogVisibleProp(mUIDialog, getActivity()));

        // AsyncCommand<Void, TweetPageResponse> findMoreCommand = AsyncCommand.create(new Func1<Void,
        // Observable<TweetPageResponse>>() {
        // public Observable<TweetPageResponse> call(Void pVoid) {
        // return mTwitterRepository.findTweets(mTimeline, mTimeline.pastGap(), 1, 20);
        // }
        // });
        // findMoreCommand.map(new Func1<TweetPageResponse, TweetPage>() {
        // public TweetPage call(TweetPageResponse pTweetPageResponse) {
        // return pTweetPageResponse.tweetPage();
        // }
        // }).subscribe(RxUI.toListView(mUIListAdapter));

        FragmentManager lFragmentManager = getFragmentManager();
        mPresenter = (NewsPresenter) lFragmentManager.findFragmentByTag("presenter");
        if (mPresenter == null) {
            mPresenter = NewsPresenter.forUser(getArguments().getString(ARG_SCREEN_NAME));
            lFragmentManager.beginTransaction().add(mPresenter, "presenter").commit();
        }
        mPresenter.setTargetFragment(this, 0);

        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());
        return lUIFragment;
    }

    public void onInitializeInstanceState(Bundle pBundle) {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }

    @Override
    public void onBind(PageIndex<News> pIndex) {
        mUIListAdapter.bindTo(mPresenter.tweets());
        mPresenter.tweets().onInsert().subscribe(RxUI.notifyListView(mUIListAdapter));

        Observable<Void> onMoreAction = RxUI.fromOnMoreAction(mUIListAdapter)/* .startWith(RxUI.VOID_SIGNALS) */;
        onMoreAction.subscribe(mPresenter.findMoreCommand());
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
        // Observable<TweetPageResponse> findTweets = mTwitterRepository.findTweets(mTimeline, mTimeline.pastGap(), 1, 20)
        // .observeOn(AndroidScheduler.threadForUI());
        // bindToList(findTweets, mUIListAdapter);
        // Observable<Boolean> showProgress = Observable.from(Boolean.FALSE).startWith(Boolean.TRUE);
        // showProgress(showProgress);
        // new ListViewBinder(mUIList, mUIListAdapter, new PageIndex<News>()).bind(lFindTweets);
        // lTweetPageResponses = updateTimeline(lTweetPageResponses, mTimeline);
        // lFindTweets = updateList(lFindTweets, mUIListAdapter);
    }

    // public void showDialog(final Observable<TweetPageResponse> pTweetResponses) {
    // // mLoadingMore = true;
    // mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);
    //
    // pTweetResponses.subscribe(new Observer<TweetPageResponse>() {
    // public void onNext(TweetPageResponse pTweetPageResponse) {
    // }
    //
    // public void onCompleted() {
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
