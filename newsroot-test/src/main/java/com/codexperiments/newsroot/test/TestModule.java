package com.codexperiments.newsroot.test;

import javax.inject.Singleton;

import android.content.Context;

import com.codexperiments.newsroot.common.Application;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
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
    public TwitterManager provideTwitterManager(@Application Context pContext) {
        return new TwitterManager(pContext, new TwitterManager.Config() {
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
