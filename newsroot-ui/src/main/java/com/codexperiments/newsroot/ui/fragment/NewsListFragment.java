package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.app.ProgressDialog;
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
import com.codexperiments.newsroot.common.structure.RxPageIndex;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TimeRange;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.presentation.NewsPresentation;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.robolabor.task.TaskManager;

public class NewsListFragment extends Fragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterRepository mTwitterRepository;

    private PageAdapter<NewsPresentation> mUIListAdapter;
    private ListView mUIList;
    private ProgressDialog mUIDialog;

    // private NewsListPresentation mPresentation;
    private Timeline mTimeline;
    private RxPageIndex<NewsPresentation> mTweets;
    private TimeRange mTimeRange;

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
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterRepository = BaseApplication.getServiceFrom(getActivity(), TwitterRepository.class);

        mUIDialog = new ProgressDialog(getActivity());
        mUIDialog.setTitle("Please wait...");
        mUIDialog.setMessage("Retrieving tweets ...");
        mUIDialog.setIndeterminate(true);

        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);

        mUIListAdapter = new PageAdapter<NewsPresentation>(pLayoutInflater);
        mUIListAdapter.addItemType(NewsTimeGapItem.Model.class, R.layout.item_news_timegap);
        mUIListAdapter.addItemType(NewsItem.Model.class, R.layout.item_news);
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setChoiceMode(AbsListView.CHOICE_MODE_NONE); // CHOICE_MODE_MULTIPLE
        mUIList.setAdapter(mUIListAdapter);
        mUIDialog = new ProgressDialog(getActivity());

        // FragmentManager lFragmentManager = getFragmentManager();
        // mPresentation = (NewsListPresentation) lFragmentManager.findFragmentByTag("presentation");
        // if (mPresentation == null) {
        // mPresentation = NewsListPresentation.forUser(getArguments().getString(ARG_SCREEN_NAME));
        // lFragmentManager.beginTransaction().add(mPresentation, "presentation").commit();
        // }
        // mPresentation.setTargetFragment(this, 0);
        mTimeline = mTwitterRepository.findTimeline(getArguments().getString(ARG_SCREEN_NAME));
        mTweets = RxPageIndex.newPageIndex();
        mTimeRange = null;

        mFindMoreCommand = AsyncCommand.create(new Func1<Void, Observable<TweetPageResponse>>() {
            public Observable<TweetPageResponse> call(Void pVoid) {
                return mTwitterRepository.findTweets(mTimeline, TimeGap.pastTimeGap(mTimeRange), 1, 20);
            }
        });
        mFindMoreCommand.subscribe(new Action1<TweetPageResponse>() {
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
        });

        mUIListAdapter.bindTo(mTweets);
        mTweets.onInsert().subscribe(RxUI.toListView(mUIListAdapter));
        Observable<Void> onMoreAction = RxUI.fromOnMoreAction(mUIListAdapter)/* .startWith(RxUI.VOID_SIGNALS) */;
        onMoreAction.subscribe(mFindMoreCommand);

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
    }

    @Override
    public void onStop() {
        super.onStop();
        mUIDialog.dismiss();
        mTaskManager.unmanage(this);
        mEventBus.unregisterListener(this);
    }
}
