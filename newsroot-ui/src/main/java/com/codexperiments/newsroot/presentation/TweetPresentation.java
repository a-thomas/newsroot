package com.codexperiments.newsroot.presentation;

import com.codexperiments.newsroot.domain.twitter.Tweet;

public class TweetPresentation implements NewsPresentation {
    private Tweet mTweet;
    private boolean mSelected;

    public TweetPresentation(Tweet pTweet) {
        super();
        mTweet = pTweet;
        mSelected = false;
    }

    public Tweet getTweet() {
        return mTweet;
    }
}
