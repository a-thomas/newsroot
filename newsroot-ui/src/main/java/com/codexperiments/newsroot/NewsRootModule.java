package com.codexperiments.newsroot;

import com.codexperiments.newsroot.ui.activity.HomeActivity;
import dagger.Module;

@Module(complete = true,
        injects = { HomeActivity.class })
public class NewsRootModule {
    private final android.app.Application mApplication;

    public NewsRootModule(android.app.Application pApplication) {
        mApplication = pApplication;
    }
}
