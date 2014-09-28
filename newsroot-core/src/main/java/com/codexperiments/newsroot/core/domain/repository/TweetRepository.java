package com.codexperiments.newsroot.core.domain.repository;

import rx.Observable;

public interface TweetRepository {
    Observable<Object> findHomeTweets();
}
