package com.codexperiments.newsroot.repository;

import com.codexperiments.newsroot.api.entity.Tweet;

import java.util.List;

public interface TweetRepository {
    List<Tweet> findTweets();
}
