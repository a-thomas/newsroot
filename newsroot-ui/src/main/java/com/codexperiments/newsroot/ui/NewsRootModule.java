package com.codexperiments.newsroot.ui;

import com.codexperiments.newsroot.common.AndroidModule;
import com.codexperiments.newsroot.common.platform.PlatformModule;
import com.codexperiments.newsroot.ui.activity.HomeActivity;
import com.codexperiments.newsroot.ui.fragment.AuthorizationFragment;
import com.codexperiments.newsroot.ui.fragment.NewsListFragment;

import dagger.Module;

/**
 * Class used to abstract platform-specific set-up, behavior or anything else.
 */
@Module(includes = { AndroidModule.class, PlatformModule.class }, //
        injects = { HomeActivity.class, AuthorizationFragment.class, NewsListFragment.class })
public class NewsRootModule {
}
