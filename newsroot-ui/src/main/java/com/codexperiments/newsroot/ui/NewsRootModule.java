package com.codexperiments.newsroot.ui;

import android.content.Context;

import com.codexperiments.newsroot.common.Application;
import com.codexperiments.newsroot.common.platform.PlatformModule;
import com.codexperiments.newsroot.ui.activity.HomeActivity;
import com.codexperiments.newsroot.ui.fragment.AuthorizationFragment;
import com.codexperiments.newsroot.ui.fragment.NewsListFragment;

import dagger.Module;
import dagger.Provides;

/**
 * Class used to abstract platform-specific set-up, behavior or anything else.
 */
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
}
