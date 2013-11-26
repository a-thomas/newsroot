package com.codexperiments.newsroot.test;

import javax.inject.Singleton;

import android.content.Context;

import com.codexperiments.newsroot.common.Application;
import com.codexperiments.newsroot.manager.tweet.TweetManager;
import com.codexperiments.newsroot.test.server.MockServer;
import com.codexperiments.newsroot.ui.NewsRootModule;

import dagger.Module;
import dagger.Provides;

@Module(includes = NewsRootModule.class, //
        library = true,
        overrides = true)
public class TestModule {
    @Provides
    @Singleton
    public TweetManager provideTweetManager(@Application Context pContext) {
        return new TweetManager(pContext, new TweetManager.Config() {
            public String getHost() {
                return "http://localhost:" + MockServer.PORT + "/";
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
}
