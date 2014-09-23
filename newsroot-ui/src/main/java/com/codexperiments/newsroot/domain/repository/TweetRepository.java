package com.codexperiments.newsroot.domain.repository;

import com.codexperiments.newsroot.domain.entity.Tweet;

import java.util.List;

public interface TweetRepository {
    List<Tweet> findTweets();
}
