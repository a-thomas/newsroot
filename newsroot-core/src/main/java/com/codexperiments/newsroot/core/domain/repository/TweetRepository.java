package com.codexperiments.newsroot.core.domain.repository;

import com.codexperiments.newsroot.core.domain.entities.Tweet;

public interface TweetRepository {
    void save(Tweet tweet);

    void delete(Tweet tweet);

    Tweet byId(long tweetId);

//    public FindAllTweet findAll();
//
//    public interface FindAllTweet /*extends Query<Tweet>*/ {
//        FindAllTweet withUser();
//
//        FindAllTweet pagedBy(int pPageSize);
//    }
}
