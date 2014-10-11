package com.codexperiments.newsroot.ui;

import com.codexperiments.newsroot.ui.authentication.AuthorizationFragment;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = false, library = true,
        injects = {AuthorizationFragment.class,})
public class UIModule {
    @Provides @Singleton
    Bus provideEventBus() {
        return new Bus();
    }
}