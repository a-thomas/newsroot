package com.codexperiments.newsroot.core.domain.repository;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import rx.Observable;

import java.util.List;

public interface TweetRepository {
    public FindAllTweet findAll();

    public interface FindAllTweet {
        public FindAllTweet withUser();

        public FindAllTweet pagedBy(int pPageSize);

        public Tweet[] asArray();

        public List<Tweet> asList();

        public Observable<Tweet> asObservable();
    }
}
