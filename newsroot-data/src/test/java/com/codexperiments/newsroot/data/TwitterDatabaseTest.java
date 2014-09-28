package com.codexperiments.newsroot.data;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import rx.functions.Action1;
import rx.observables.BlockingObservable;

@RunWith(RobolectricTestRunner.class)
public class TwitterDatabaseTest {
    TwitterDatabase database;
    TweetDAO tweetDAO;

    @Before
    public void setUp() {
        database = new TwitterDatabase(Robolectric.application);
        tweetDAO = new TweetDAO(database);
    }

    @Test
    public void testDatabase() throws Exception {
        database.executeScriptFromAssets("sql/default.sql");

        BlockingObservable.from(tweetDAO.findAll().withUser().asObservable()).forEach(new Action1<Tweet>() {
            public void call(Tweet tweet) {
                System.out.println(tweet);
            }
        });

    }
}