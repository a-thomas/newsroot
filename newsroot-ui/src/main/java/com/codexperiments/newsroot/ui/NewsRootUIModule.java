package com.codexperiments.newsroot.ui;

import com.codexperiments.newsroot.ui.authentication.AuthorizationFragment;
import com.codexperiments.newsroot.ui.timeline.TimecardFragment;
import com.codexperiments.newsroot.ui.timeline.TimelineFragment;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = false, library = true,
        injects = {AuthorizationFragment.class, TimelineFragment.class, TimecardFragment.class})
public class NewsRootUIModule {
    @Provides @Singleton
    Bus provideEventBus() {
        return new Bus();
    }
}