package com.codexperiments.newsroot;

import android.app.Activity;
import android.app.Application;
import android.os.StrictMode;
import dagger.ObjectGraph;

public class NewsRootApplication extends Application {
    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                                           .detectAll()
                                           .penaltyLog()
                                           .penaltyDialog()
                                           .penaltyDeath()
                                           .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                                       .detectAll()
                                       .penaltyLog()
                                       .penaltyDeath()
                                       .build());

        objectGraph = ObjectGraph.create(new NewsRootModule(this));
    }

    public static ObjectGraph from(Activity activity) {
        if (activity == null) throw new IllegalArgumentException("Activity is null");

        NewsRootApplication application = (NewsRootApplication) activity.getApplication();
        return application.objectGraph;
    }
}
