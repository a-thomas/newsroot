package com.codexperiments.newsroot.api.remote;

import com.codexperiments.newsroot.api.entity.Tweet;
import com.codexperiments.newsroot.api.parser.TwitterParser;
import com.codexperiments.newsroot.repository.TweetRepository;

import java.util.List;

public class TweetRemoteRepository implements TweetRepository {
    private TwitterParser parser;

    public TweetRemoteRepository(TwitterParser parser) {
        this.parser = parser;
    }

    @Override
    public List<Tweet> findTweets() {
        return parser.parseTweetList("");
    }
}
