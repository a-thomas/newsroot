package com.codexperiments.newsroot.core.provider;

import java.util.List;

public class TimelineViewModel {
    public List<TweetItemViewModel> tweetItems;

    public TimelineViewModel(List<TweetItemViewModel> tweetItems) {
        this.tweetItems = tweetItems;
    }
}
