package com.codexperiments.newsroot.data.remote;

import com.codexperiments.newsroot.data.remote.parser.TwitterParser;
import com.codexperiments.newsroot.domain.entity.Tweet;
import com.codexperiments.newsroot.domain.repository.TweetRepository;

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
