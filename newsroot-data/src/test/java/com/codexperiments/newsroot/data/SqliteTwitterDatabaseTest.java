package com.codexperiments.newsroot.data;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.data.sqlite.SqliteTweetRepository;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import rx.functions.Action1;

import static com.codexperiments.quickdao.sqlite.SQLiteRetriever.asObservable;
import static rx.observables.BlockingObservable.from;

@RunWith(RobolectricTestRunner.class)
public class SqliteTwitterDatabaseTest {
    SqliteTwitterDatabase database;
    TweetRepository tweetRepository;

    @Before
    public void setUp() {
        database = new SqliteTwitterDatabase(Robolectric.application);
        tweetRepository = new SqliteTweetRepository(database);
    }

    @Test
    public void testDatabase() throws Exception {
        database.executeScriptFromAssets("sql/default.sql");

        from(tweetRepository.findAll().withUser().retrieve(asObservable(Tweet.class))).forEach(new Action1<Tweet>() {
            public void call(Tweet tweet) {
                System.out.println(tweet);
            }
        });
    }
}