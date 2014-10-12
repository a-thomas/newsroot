package com.codexperiments.newsroot.api;

import com.codexperiments.newsroot.api.mapper.JsonConverter;
import com.codexperiments.newsroot.api.mapper.JsonMapper;
import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.converter.Converter;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Requires additional providers:
 * <ul>
 *     <li>String @Named("twitter.api.applicationKey")</li>
 *     <li>String @Named("twitter.api.applicationSecret")</li>
 * </ul>
 */
@Module(complete = false, library =  true)
public class TwitterAPIModule {
    @Provides @Singleton
    public Endpoint provideEndpoint() {
        return Endpoints.newFixedEndpoint("https://api.twitter.com");
    }

    @Provides @Singleton
    public TwitterAuthorizer provideAuthorizer(Endpoint endpoint,
                                @Named("twitter.api.applicationKey") String consumerKey,
                                @Named("twitter.api.applicationSecret") String consumerSecret) {
        return new TwitterAuthorizer(endpoint.getUrl(), consumerKey, consumerSecret);
    }

    @Provides @Singleton
    public Client provideClient(final TwitterAuthorizer authorizer) {
        return new OkClient() {
            @Override
            protected HttpURLConnection openConnection(Request request) throws IOException {
                HttpURLConnection connection = super.openConnection(request);
                authorizer.sign(connection);
                return connection;
            }
        };
    }

    @Provides @Singleton
    public Converter provideConverter(final JsonMapper parser) {
        return new JsonConverter(parser);
    }

    @Provides @Singleton
    public JsonMapper provideParser() {
        return new JsonMapper();
    }

    @Provides @Singleton
    public RestAdapter provideRestAdapter(Client client, Converter converter, Endpoint endpoint) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setClient(client)
                .setConverter(converter)
                .setEndpoint(endpoint)
                .build();
        restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
        return restAdapter;
    }

    @Provides @Singleton
    public TwitterAPI provideTwitterAPI(RestAdapter restAdapter) {
        return restAdapter.create(TwitterAPI.class);
    }
}
