package com.codexperiments.newsroot.core.domain.repository;

import com.codexperiments.newsroot.core.domain.entities.Tweet;

public interface TweetRepository {
    void feed(Tweet tweet);

    void save(Tweet tweet) throws AlreadyExistsException, ChangedMeanwhileException;

    void delete(Tweet tweet) throws ChangedMeanwhileException;

    Tweet byId(long tweetId) throws DoesNotExistException;

//    public FindAllTweet findAll();
//
//    public interface FindAllTweet /*extends Query<Tweet>*/ {
//        FindAllTweet withUser();
//
//        FindAllTweet pagedBy(int pPageSize);
//    }
}
