package com.codexperiments.newsroot.ui;

import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

import com.codexperiments.newsroot.common.ApplicationContext;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.InternalException;
import com.codexperiments.newsroot.common.platform.PlatformModule;

import dagger.ObjectGraph;

public class NewsRootApplication extends BaseApplication {
    private boolean mReady;
    private ObjectGraph mDependencies;

    public static ApplicationContext from(Activity pActivity) {
        if (pActivity != null) {
            Application lApplication = pActivity.getApplication();
            if ((lApplication != null) && (lApplication instanceof BaseApplication)) {
                return ((ApplicationContext) lApplication);
            }
        }
        throw InternalException.invalidConfiguration("Could not retrieve configuration from Activity");
    }

    public static ApplicationContext from(Application pApplication) {
        if ((pApplication != null) && (pApplication instanceof BaseApplication)) {
            return ((ApplicationContext) pApplication);
        }
        throw InternalException.invalidConfiguration("Could not retrieve configuration from Activity");
    }

    public NewsRootApplication() {
        super();
        mReady = false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mDependencies = ObjectGraph.create(new PlatformModule(), //
                                           new NewsRootModule(this));
        mReady = true;
    }

    @Override
    public ObjectGraph dependencies() {
        return mDependencies;
    }

    public void resetDependenciesForTestPurpose(final ObjectGraph pDependencies) {
        final CountDownLatch lLatch = new CountDownLatch(1);
        new Handler(this.getMainLooper()).post(new Runnable() {
            public void run() {
                if (!mReady) {
                    resetDependenciesForTestPurpose(pDependencies);
                } else {
                    mDependencies = pDependencies;
                    lLatch.countDown();
                }
            }
        });
        try {
            lLatch.await();
        } catch (InterruptedException eInterruptedException) {
            throw new IllegalStateException(eInterruptedException);
        }
    }
}
