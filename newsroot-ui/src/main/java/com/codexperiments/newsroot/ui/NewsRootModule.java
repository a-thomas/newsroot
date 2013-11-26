package com.codexperiments.newsroot.ui;

import javax.inject.Singleton;

import android.content.Context;

import com.codexperiments.newsroot.common.Application;
import com.codexperiments.newsroot.common.platform.PlatformModule;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.newsroot.repository.twitter.TwitterDatabaseRepository;
import com.codexperiments.newsroot.repository.twitter.TwitterRemoteRepository;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.newsroot.ui.activity.HomeActivity;
import com.codexperiments.newsroot.ui.fragment.AuthorizationFragment;
import com.codexperiments.newsroot.ui.fragment.NewsListFragment;

import dagger.Module;
import dagger.Provides;

@Module(complete = true, //
        includes = { PlatformModule.class },
        injects = { HomeActivity.class, AuthorizationFragment.class, NewsListFragment.class })
public class NewsRootModule {
    private final android.app.Application mApplication;

    public NewsRootModule(android.app.Application pApplication) {
        mApplication = pApplication;
    }

    @Provides
    @Application
    public Context provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    public TwitterManager provideTwitterManager(@Application Context pContext) {
        return new TwitterManager(pContext, new TwitterManager.Config() {
            public String getHost() {
                return "https://api.twitter.com/";
            }

            public String getConsumerKey() {
                return "3Ng9QGTB7EpZCHDOIT2jg";
            }

            public String getConsumerSecret() {
                return "OolXzfWdSF6uMdgt2mvLNpDl4HOA1JNlN487LvDUA4";
            }

            public String getCallbackURL() {
                return "oauth://newsroot-callback";
            }
        });
    }

    @Provides
    @Singleton
    public TwitterDatabase provideTwitterDatabase(@Application Context pContext) {
        return new TwitterDatabase(pContext);
    }

    @Provides
    @Singleton
    public TwitterRepository provideTwitterRepository(TwitterManager pTwitterManager, TwitterDatabase pTwitterDatabase) {
        TwitterRepository lRemoteRepository = new TwitterRemoteRepository(pTwitterManager, "https://api.twitter.com/");
        return new TwitterDatabaseRepository(pTwitterDatabase, lRemoteRepository);
    }
}
