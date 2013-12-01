package com.codexperiments.newsroot.ui;

import javax.inject.Singleton;

import android.content.Context;

import com.codexperiments.newsroot.common.Application;
import com.codexperiments.newsroot.common.platform.PlatformModule;
import com.codexperiments.newsroot.data.tweet.TweetDatabase;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.repository.tweet.TweetDatabaseRepository;
import com.codexperiments.newsroot.repository.tweet.TweetRemoteRepository;
import com.codexperiments.newsroot.repository.tweet.TweetRepository;
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
    public TweetManager provideTweetManager(@Application Context pContext) {
        return new TweetManager(pContext, new TweetManager.Config() {
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
    public TweetDatabase provideTweetDatabase(@Application Context pContext) {
        return new TweetDatabase(pContext);
    }

    @Provides
    @Singleton
    public TweetRepository provideTweetRepository(TweetManager pTweetManager, TweetDatabase pTweetDatabase) {
        TweetRepository lRemoteRepository = new TweetRemoteRepository(pTweetManager, "https://api.twitter.com/");
        return new TweetDatabaseRepository(pTweetDatabase, lRemoteRepository);
    }
}
