package com.codexperiments.newsroot.ui.fragment;

import rx.Observer;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.newsroot.ui.activity.AndroidScheduler;
import com.codexperiments.robolabor.task.TaskManager;

public class NewsFragment extends Fragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterRepository mTwitterRepository;

    private Timeline mTimeline;
    // private List<News> mTweets;
    // private boolean mFromCache;
    // private boolean mHasMore; // TODO
    private boolean mLoadingMore;

    private NewsAdapter mUIListAdapter;
    private ListView mUIList;
    private ProgressDialog mUIDialog;

    public static final NewsFragment forUser(String pScreenName) {
        NewsFragment lFragment = new NewsFragment();
        Bundle lArguments = new Bundle();
        lArguments.putString(ARG_SCREEN_NAME, pScreenName);
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(LayoutInflater pLayoutInflater, ViewGroup pContainer, Bundle pBundle) {
        super.onCreateView(pLayoutInflater, pContainer, pBundle);
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterRepository = BaseApplication.getServiceFrom(getActivity(), TwitterRepository.class);

        mTimeline = new Timeline(getArguments().getString(ARG_SCREEN_NAME));
        // mTweets = new ArrayList<News>(); // TODO Get from prefs.
        // mFromCache = true;
        // mHasMore = true;
        mLoadingMore = false;

        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);
        mUIListAdapter = new NewsAdapter(pLayoutInflater, mTimeline, new NewsAdapter.Callback() {
            public void onMore() {
                moreTweets();
            }

            public void onRefresh() {
                refreshTweets();
            }
        });
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
        moreTweets();
    }

    @Override
    public void onStop() {
        super.onStop();
        mUIDialog.dismiss();
        mTaskManager.unmanage(this);
        mEventBus.unregisterListener(this);
    }

    public void refreshTweets() {
        mTwitterRepository.findLatestNews(mTimeline)
                          .observeOn(AndroidScheduler.getInstance())
                          .subscribe(new Observer<TweetPage>() {
                              public void onNext(TweetPage pTweetPage) {
                                  mTimeline.addTweets(pTweetPage);
                                  mUIListAdapter.notifyDataSetChanged();
                              }

                              public void onCompleted() {
                                  // Nothing to do.
                                  mUIDialog.dismiss();
                              }

                              public void onError(Throwable pThrowable) {
                                  mUIDialog.dismiss();
                                  Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                                  pThrowable.printStackTrace();
                              }
                          });
    }

    public void moreTweets() {
        if (!mLoadingMore) {
            mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);

            mLoadingMore = true;
            mTwitterRepository.findOlderNewsFromCache(mTimeline)
                              .observeOn(AndroidScheduler.getInstance())
                              .subscribe(new Observer<TweetPage>() {
                                  public void onNext(TweetPage pTweetPage) {
                                      mTimeline.addTweets(pTweetPage);
                                      mUIListAdapter.notifyDataSetChanged();
                                  }

                                  public void onCompleted() {
                                      mLoadingMore = false;
                                      mUIDialog.dismiss();
                                  }

                                  public void onError(Throwable pThrowable) {
                                      mUIDialog.dismiss();
                                      Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                                      pThrowable.printStackTrace();
                                  }
                              });
        }
    }
}
