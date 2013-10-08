package com.codexperiments.newsroot.ui.fragment;

import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.rx.AsyncCommand;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.common.structure.PageIndex;
import com.codexperiments.newsroot.common.structure.RxPageIndex;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.TimeRange;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.domain.twitter.TweetPage;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;

public class NewsPresenter extends Fragment {
    private static final String ARG_SCREEN_NAME = "screenName";

    private NewsView mView;
    private TwitterRepository mTwitterRepository;

    private Timeline mTimeline;
    private RxPageIndex<News> mTweets;
    private TimeRange mTimeRange;

    private AsyncCommand<Void, TweetPageResponse> mFindMoreCommand;

    public static final NewsPresenter forUser(String pScreenName) {
        NewsPresenter lPresenter = new NewsPresenter();
        Bundle lArguments = new Bundle();
        lArguments.putString(ARG_SCREEN_NAME, pScreenName);
        lPresenter.setArguments(lArguments);
        return lPresenter;
    }

    protected NewsPresenter() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mTwitterRepository = BaseApplication.getServiceFrom(getActivity(), TwitterRepository.class);
        mTimeline = mTwitterRepository.findTimeline(getArguments().getString(ARG_SCREEN_NAME));
        mTweets = RxPageIndex.newPageIndex();
        mTimeRange = null;
    }

    @Override
    public void setTargetFragment(Fragment pFragment, int pRequestCode) {
        super.setTargetFragment(pFragment, pRequestCode);
        mView = (NewsView) pFragment;
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        View lContentView = super.onCreateView(pInflater, pContainer, pSavedInstanceState);
        return lContentView;
    }

    @Override
    public void onActivityCreated(Bundle pSavedInstanceState) {
        super.onActivityCreated(pSavedInstanceState);
        mView.onBind(mTweets);

        mFindMoreCommand.onNext(RxUI.VOID_SIGNAL);
    }

    public RxPageIndex<News> tweets() {
        return mTweets;
    }

    public AsyncCommand<Void, TweetPageResponse> findMoreCommand() {
        if (mFindMoreCommand == null) {
            mFindMoreCommand = AsyncCommand.create(new Func1<Void, Observable<TweetPageResponse>>() {
                public Observable<TweetPageResponse> call(Void pVoid) {
                    return mTwitterRepository.findTweets(mTimeline, TimeGap.pastTimeGap(mTimeRange), 1, 20);
                }
            });

            mFindMoreCommand.subscribe(new Action1<TweetPageResponse>() {
                public void call(TweetPageResponse pTweetPageResponse) {
                    TweetPage lPage = pTweetPageResponse.tweetPage();
                    mTimeRange = TimeRange.append(mTimeRange, lPage.tweets());
                    mTweets.insert(lPage);
                }
            });
        }
        return mFindMoreCommand;
    }

    @Override
    public void onAttach(Activity pActivity) {
        super.onAttach(pActivity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface NewsView {
        void onBind(PageIndex<News> pIndex);
    }
}