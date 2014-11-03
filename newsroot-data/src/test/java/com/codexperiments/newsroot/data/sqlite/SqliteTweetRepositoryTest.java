package com.codexperiments.newsroot.data.sqlite;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.newsroot.core.domain.repository.*;
import com.codexperiments.newsroot.test.TweetData;
import com.codexperiments.newsroot.test.UserData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import static com.codexperiments.newsroot.core.domain.entities.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SqliteTweetRepositoryTest {
    SqliteTwitterDatabase database;
    UserRepository userRepository;
    TweetRepository tweetRepository;

    @Before
    public void setUp() throws IOException {
        database = new SqliteTwitterDatabase(Robolectric.application);
        userRepository = new SqliteUserRepository(database);
        tweetRepository = new SqliteTweetRepository(database);

        database.executeScriptFromAssets("sql/default.sql");
    }

    @Test
    public void testTweetById() throws DoesNotExistException {
        Tweet tweet = tweetRepository.byId(TweetData.TWEET_1_CLEMENT);
        assertThat(tweet).hasId(TweetData.TWEET_1_CLEMENT)
                         .hasVersion(1)
                         .hasCreatedAt(0);
        assertThat(tweet.getText()).contains("Clément");
        assertThat(tweet.getUser()).hasId(1)
                                   .hasName("Le Monde")
                                   .hasScreenName("lemondefr");
    }

    @Test(expected = DoesNotExistException.class)
    public void testTweetById_doesNotExist() throws DoesNotExistException {
        tweetRepository.byId(-1);
    }

    @Test
    public void testSaveTweet_create_success() throws Exception {
        // GIVEN I have a new Tweet.
        User user = userRepository.byId(UserData.USER_1_LEMONDE);
        Tweet tweetToSave = TweetData.createTweet(user);
        long tweetToSaveVersion = tweetToSave.getVersion();

        // WHEN I save it.
        tweetRepository.save(tweetToSave);

        // THEN The tweet is saved in the repository
        // AND its version is updated.
        Tweet savedTweet = tweetRepository.byId(tweetToSave.getId());
        assertThat(savedTweet).hasId(tweetToSave.getId())
                              .hasVersion(tweetToSave.getVersion()).hasVersion(tweetToSaveVersion + 1)
                              .hasCreatedAt(tweetToSave.getCreatedAt())
                              .hasText(tweetToSave.getText())
                              .hasUser(user);
    }

    @Test(expected = AlreadyExistsException.class)
    public void testSaveTweet_create_alreadyExists() throws AlreadyExistsException, ChangedMeanwhileException, DoesNotExistException {
        User user = UserData.createUser(UserData.USER_1_LEMONDE, 1);
        Tweet tweet = TweetData.createTweet(TweetData.TWEET_1_CLEMENT, 0, user);
        tweetRepository.save(tweet);
    }

    @Test
    public void testSaveTweet_update() throws Exception {
        String newText = "New text";

        // GIVEN I have a new Tweet.
        Tweet tweetToSave = tweetRepository.byId(TweetData.TWEET_1_CLEMENT);
        long tweetToSaveVersion = tweetToSave.getVersion();

        // WHEN I change and save it.
        tweetToSave.setText(newText);
        tweetRepository.save(tweetToSave);

        // THEN The tweet is saved in the repository
        // AND its version is updated.
        Tweet savedTweet = tweetRepository.byId(tweetToSave.getId());
        assertThat(savedTweet).hasId(tweetToSave.getId())
                              .hasVersion(tweetToSave.getVersion()).hasVersion(tweetToSaveVersion + 1)
                              .hasCreatedAt(tweetToSave.getCreatedAt())
                              .hasText(newText)
                              .hasUser(tweetToSave.getUser());
    }

    @Test(expected = ChangedMeanwhileException.class)
    public void testSaveTweet_update_changedMeanwhile() throws AlreadyExistsException, ChangedMeanwhileException, DoesNotExistException {
        User user = UserData.createUser(UserData.USER_1_LEMONDE, 1);
        Tweet tweet = TweetData.createTweet(TweetData.TWEET_1_CLEMENT, 2, user);
        tweetRepository.save(tweet);
    }

    @Test
    public void testFeedTweet_alreadyExists() throws AlreadyExistsException, ChangedMeanwhileException, DoesNotExistException {
        User user = UserData.createUser(UserData.USER_1_LEMONDE, 1);
        Tweet tweet = TweetData.createTweet(TweetData.TWEET_1_CLEMENT, 0, user);
        tweetRepository.feed(tweet);
    }
}