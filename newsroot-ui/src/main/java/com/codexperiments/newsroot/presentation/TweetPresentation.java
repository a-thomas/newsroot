package com.codexperiments.newsroot.presentation;

import rx.Observable;

import com.codexperiments.newsroot.common.rx.BooleanProperty;
import com.codexperiments.newsroot.common.rx.Command;
import com.codexperiments.newsroot.domain.twitter.Tweet;

public class TweetPresentation implements NewsPresentation {
    private Tweet mTweet;
    private BooleanProperty mSelected;

    private Command<Void, Boolean> mToggleSelection;

    public TweetPresentation(Tweet pTweet) {
        super();
        mTweet = pTweet;
        mSelected = BooleanProperty.create();
    }

    public Tweet getTweet() {
        return mTweet;
    }

    public Command<Void, ?> toggleSelection() {
        if (mToggleSelection == null) {
            mToggleSelection = Command.create(mSelected.toggle());
            mToggleSelection.subscribe(mSelected);
        }
        return mToggleSelection;
    }
    
    public Observable<Boolean> isSelected() {
        return mSelected;
    }
}
