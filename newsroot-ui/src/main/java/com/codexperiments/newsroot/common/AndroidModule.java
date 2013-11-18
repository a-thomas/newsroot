package com.codexperiments.newsroot.common;

import android.content.Context;
import dagger.Module;
import dagger.Provides;

/**
 * Class used to abstract platform-specific set-up, behaviour or anything else.
 */
@Module(library = true)
public class AndroidModule {
    private android.app.Application mApplication;

    public AndroidModule(android.app.Application pApplication) {
        mApplication = pApplication;
    }

    @Provides
    @Application
    public Context provideApplication() {
        return mApplication;
    }
}
