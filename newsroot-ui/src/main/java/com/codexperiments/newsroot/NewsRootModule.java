package com.codexperiments.newsroot;

import android.app.Application;
import com.codexperiments.newsroot.api.TwitterModule;
import com.codexperiments.newsroot.ui.UIModule;
import com.codexperiments.newsroot.ui.HomeActivity;
import com.codexperiments.newsroot.ui.timeline.TimelineFragment;
import com.codexperiments.newsroot.ui.authentication.AuthorizationFragment;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;

@Module(complete = true, overrides = true,
        includes = {TwitterModule.class, UIModule.class},
        injects = {HomeActivity.class, AuthorizationFragment.class, TimelineFragment.class})
public class NewsRootModule {
    private final Application application;

    public NewsRootModule(Application application) {
        this.application = application;
    }

    @Provides @Named("twitter.api.applicationKey")
    public String provideApplicationKey() {
        return application.getString(R.string.twitter_applicationKey);
    }

    @Provides @Named("twitter.api.applicationSecret")
    public String provideApplicationSecret() {
        return application.getString(R.string.twitter_applicationSecret);
    }
}
