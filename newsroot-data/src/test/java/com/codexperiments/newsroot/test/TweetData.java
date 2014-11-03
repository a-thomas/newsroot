package com.codexperiments.newsroot.test;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.entities.User;

import java.util.Date;
import java.util.UUID;

public class TweetData {
    public static long TWEET_1_CLEMENT = 349443871694012400L;

    private static volatile long idSequence = 0;

    public static Tweet createTweet(User user) {
        return createTweet(++idSequence, 0, user);
    }

    public static Tweet createTweet(long id, long version, User user) {
        UUID uuid = UUID.randomUUID();

        Tweet tweet = new Tweet();
        tweet.setId(id);
        tweet.setCreatedAt(new Date().getTime());
        tweet.setText("Tweet " + uuid.toString());
        tweet.setUser(user);
        tweet.setVersion(version);
        return tweet;
    }
}
