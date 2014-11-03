package com.codexperiments.newsroot.core.provider;

import rx.Observable;

public interface TimelineProvider {
    public Observable<TimelineViewModel> findTweets();
}
