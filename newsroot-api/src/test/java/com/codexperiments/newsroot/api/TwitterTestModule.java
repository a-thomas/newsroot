package com.codexperiments.newsroot.api;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;

import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Module(complete = false, includes = {TwitterAPIModule.class}, library = true, overrides = true)
public class TwitterTestModule {
    private Properties properties = new Properties();

    public TwitterTestModule() {
        try {
            properties.load(new FileInputStream("./twitter.properties"));
            return;
        } catch (IOException ioException) {
            // thrown from next try/catch.
        }

        try {
            properties.load(new FileInputStream("../twitter.properties"));
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    @Provides @Singleton
    public TwitterAuthorizer provideAuthorizer(Endpoint endpoint) {
        TwitterAuthorizer authorizer = new TwitterAuthorizer(endpoint.getUrl(),
                                                             properties.getProperty("twitter.api.applicationKey"),
                                                             properties.getProperty("twitter.api.applicationSecret"));
        authorizer.authorize(properties.getProperty("twitter.api.userToken"), properties.getProperty("twitter.api.userSecret"));
        return authorizer;
    }
}