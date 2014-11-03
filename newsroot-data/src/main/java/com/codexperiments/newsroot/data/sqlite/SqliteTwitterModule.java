package com.codexperiments.newsroot.data.sqlite;

import android.app.Application;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.core.domain.repository.UserRepository;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = false, library =  true)
public class SqliteTwitterModule {

    @Provides @Singleton
    public SqliteTwitterDatabase provideTwitterDatabase(Application application) {
        return new SqliteTwitterDatabase(application);
    }

    @Provides @Singleton
    public TweetRepository provideTweetRepository(SqliteTwitterDatabase twitterDatabase) {
        return new SqliteTweetRepository(twitterDatabase);
    }

    @Provides @Singleton
    public UserRepository provideUserRepository(SqliteTwitterDatabase twitterDatabase) {
        return new SqliteUserRepository(twitterDatabase);
    }
}
