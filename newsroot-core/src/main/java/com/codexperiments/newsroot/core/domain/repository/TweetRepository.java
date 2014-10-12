package com.codexperiments.newsroot.core.domain.repository;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.quickdao.Query;

public interface TweetRepository {
    public FindAllTweet findAll();

    public interface FindAllTweet extends Query<Tweet> {
        FindAllTweet withUser();

        FindAllTweet pagedBy(int pPageSize);
    }
}
