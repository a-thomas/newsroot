package com.codexperiments.newsroot.data.remote;

import android.app.Application;
import com.codexperiments.newsroot.api.TwitterAPI;
import com.codexperiments.newsroot.api.TwitterAPIModule;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = false, library =  true, includes = {TwitterAPIModule.class})
public class RemoteTwitterModule {
    @Provides @Singleton
    public TweetRepository provideTweetRepository(TwitterAPI twitterAPI) {
        return new RemoteTweetRepository(twitterAPI);
    }
}
