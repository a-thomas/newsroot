package com.codexperiments.newsroot;

import android.app.Application;
import com.codexperiments.newsroot.api.TwitterAPIModule;
import com.codexperiments.newsroot.core.service.TimelineSync;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterModule;
import com.codexperiments.newsroot.presenter.timeline.TimelineProvider;
import com.codexperiments.newsroot.ui.HomeActivity;
import com.codexperiments.newsroot.ui.NewsRootUIModule;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;

@Module(complete = true, overrides = true,
        includes = {TwitterAPIModule.class, SqliteTwitterModule.class, NewsRootUIModule.class},
        injects = {HomeActivity.class})
public class NewsRootModule {
    private final Application application;

    public NewsRootModule(Application application) {
        this.application = application;
    }

    @Provides
    public Application provideApplication() {
        return application;
    }

    @Provides @Named("twitter.api.applicationKey")
    public String provideApplicationKey() {
        return application.getString(R.string.twitter_applicationKey);
    }

    @Provides @Named("twitter.api.applicationSecret")
    public String provideApplicationSecret() {
        return application.getString(R.string.twitter_applicationSecret);
    }
//
//    @Provides
//    public TimelineProvider provideTimelineProvider(SqliteTwitterDatabase datasource, TimelineSync timelineSync) {
//        return new TimelineProvider(datasource, timelineSync);
//    }
}
