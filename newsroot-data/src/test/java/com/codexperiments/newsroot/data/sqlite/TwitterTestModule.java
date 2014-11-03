package com.codexperiments.newsroot.data.sqlite;

import android.app.Application;
import com.codexperiments.newsroot.api.TwitterAPIModule;
import com.codexperiments.newsroot.api.TwitterAuthorizer;
import com.codexperiments.newsroot.data.sync.TwitterSyncModule;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import retrofit.Endpoint;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.client.Request;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Properties;

@Module(complete = false, includes = {TwitterAPIModule.class, TwitterSyncModule.class}, library = true, overrides = true)
public class TwitterTestModule {
    @Provides
    public Application provideApplication() {
        return Robolectric.application;
    }

    @Provides @Named("twitter.api.applicationKey")
    public String provideApplicationKey() {
        return "";
    }

    @Provides @Named("twitter.api.applicationSecret")
    public String provideApplicationSecret() {
        return "";
    }
}