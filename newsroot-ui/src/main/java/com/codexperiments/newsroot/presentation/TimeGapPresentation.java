package com.codexperiments.newsroot.presentation;

import rx.Observable;
import rx.util.functions.Func1;

import com.codexperiments.newsroot.common.rx.AsyncCommand;
import com.codexperiments.newsroot.common.rx.BooleanProperty;
import com.codexperiments.newsroot.common.rx.Command;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.repository.twitter.TweetPageResponse;

public class TimeGapPresentation implements NewsPresentation {
    private final TimeGap mTimeGap;

    private final BooleanProperty mLoading;
    private final Command<Void, Void> mLoadCommand;

    @SuppressWarnings("unchecked")
    public TimeGapPresentation(AsyncCommand<TimeGap, TweetPageResponse> pFindGapCommand, TimeGap pTimeGap) {
        super();
        mTimeGap = pTimeGap;
        mLoading = BooleanProperty.create();

        mLoadCommand = Command.create();

        mLoading.where(RxUI.eq(Boolean.TRUE)) //
                .map(RxUI.toConstant(mTimeGap))
                .subscribe(pFindGapCommand);

        Observable<Boolean> loadingRequested = pFindGapCommand.where(matchesTimegap()).map(RxUI.toConstant(Boolean.FALSE));
        Observable<Boolean> loadingFinished = mLoadCommand.map(RxUI.toConstant(Boolean.TRUE));
        Observable.merge(loadingRequested, loadingFinished).subscribe(mLoading); // TODO distinct.
    }

    private Func1<TweetPageResponse, Boolean> matchesTimegap() {
        return new Func1<TweetPageResponse, Boolean>() {
            public Boolean call(TweetPageResponse pTweetPageResponse) {
                return (pTweetPageResponse != null) ? pTweetPageResponse.initialGap().equals(mTimeGap) : Boolean.TRUE;
            }
        };
    }

    public TimeGap getTimeGap() {
        return mTimeGap;
    }

    public Command<Void, ?> loadCommand() {
        return mLoadCommand;
    }

    public BooleanProperty loading() {
        return mLoading;
    }
}