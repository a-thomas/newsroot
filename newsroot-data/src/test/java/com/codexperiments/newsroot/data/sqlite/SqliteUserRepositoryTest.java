package com.codexperiments.newsroot.data.sqlite;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.core.domain.repository.UserRepository;
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
public class SqliteUserRepositoryTest {
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
    public void testUserById() {
        User user = userRepository.byId(UserData.USER_1_LEMONDE);
        assertThat(user).hasId(1)
                        .hasName("Le Monde")
                        .hasScreenName("lemondefr");
    }

    @Test
    public void testSaveUser_create() throws Exception {
        // GIVEN I have a new User.
        User userToSave = UserData.createUser();
        long userToSaveVersion = userToSave.getVersion();

        // WHEN I save it.
        userRepository.save(userToSave);

        // THEN The tweet is saved in the repository
        // AND its version is updated.
        User savedUser = userRepository.byId(userToSave.getId());
        assertThat(savedUser).hasId(userToSave.getId())
                             .hasVersion(userToSave.getVersion()).hasVersion(userToSaveVersion + 1)
                             .hasName(userToSave.getName())
                             .hasScreenName(userToSave.getScreenName());
    }

    @Test
    public void testSaveUser_update() throws Exception {
        String newName = "New name";

        // GIVEN I have a new User.
        User userToSave = userRepository.byId(UserData.USER_1_LEMONDE);
        long userToSaveVersion = userToSave.getVersion();

        // WHEN I change and save it.
        userToSave.setName(newName);
        userRepository.save(userToSave);

        // THEN The tweet is saved in the repository
        // AND its version is updated.
        User savedUser = userRepository.byId(userToSave.getId());
        assertThat(savedUser).hasId(userToSave.getId())
                             .hasVersion(userToSave.getVersion()).hasVersion(userToSaveVersion + 1)
                             .hasName(userToSave.getName())
                             .hasScreenName(userToSave.getScreenName());
    }
}