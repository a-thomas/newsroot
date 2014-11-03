package com.codexperiments.newsroot.data.sqlite;

import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.newsroot.core.domain.repository.AlreadyExistsException;
import com.codexperiments.newsroot.core.domain.repository.ChangedMeanwhileException;
import com.codexperiments.newsroot.core.domain.repository.DoesNotExistException;
import com.codexperiments.newsroot.core.domain.repository.UserRepository;
import com.codexperiments.newsroot.test.UserData;
import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import javax.inject.Inject;
import java.io.IOException;

import static com.codexperiments.newsroot.core.domain.entities.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SqliteUserRepositoryTest {
    @Inject SqliteTwitterDatabase database;
    @Inject UserRepository userRepository;

    @Module(complete = true, overrides = true, includes = TwitterTestModule.class, injects = SqliteUserRepositoryTest.class)
    class LocalModule {
    }

    @Before
    public void setUp() throws IOException {
        ObjectGraph.create(new LocalModule()).inject(this);

        database.executeScriptFromAssets("sql/default.sql");
    }

    @Test
    public void testUserById() throws DoesNotExistException {
        User user = userRepository.byId(UserData.USER_1_LEMONDE);
        assertThat(user).hasId(1)
                        .hasName("Le Monde")
                        .hasScreenName("lemondefr");
    }

    @Test(expected = DoesNotExistException.class)
    public void testUserById_doesNotExist() throws DoesNotExistException {
        userRepository.byId(-1);
    }

    @Test
    public void testSaveUser_create_success() throws DoesNotExistException, AlreadyExistsException, ChangedMeanwhileException {
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

    @Test(expected = AlreadyExistsException.class)
    public void testSaveUser_create_alreadyExists() throws AlreadyExistsException, ChangedMeanwhileException, DoesNotExistException {
        User userToSave = UserData.createUser(UserData.USER_1_LEMONDE, 0);
        userRepository.save(userToSave);
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

    @Test(expected = ChangedMeanwhileException.class)
    public void testSaveUser_update_changedMeanwhile() throws AlreadyExistsException, ChangedMeanwhileException, DoesNotExistException {
        User userToSave = UserData.createUser(UserData.USER_1_LEMONDE, 2);
        userRepository.save(userToSave);
    }

    @Test
    public void testFeedUser_alreadyExists() throws AlreadyExistsException, ChangedMeanwhileException, DoesNotExistException {
        User userToSave = UserData.createUser(UserData.USER_1_LEMONDE, 0);
        userRepository.feed(userToSave);
    }
}