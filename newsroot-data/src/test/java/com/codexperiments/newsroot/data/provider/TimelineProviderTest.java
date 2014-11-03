package com.codexperiments.newsroot.data.provider;

import android.content.res.AssetManager;
import com.codexperiments.newsroot.core.provider.TimelineProvider;
import com.codexperiments.newsroot.core.provider.TimelineViewModel;
import com.codexperiments.newsroot.core.provider.TweetItemViewModel;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import com.codexperiments.newsroot.data.sqlite.TwitterTestModule;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;
import rx.functions.Action1;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static rx.observables.BlockingObservable.from;

@RunWith(RobolectricTestRunner.class)
public class TimelineProviderTest {
    @Inject SqliteTwitterDatabase dataSource;
    @Inject Client client;
    @Inject TimelineProvider timelineProvider;

    @Module(complete = true, overrides = true, includes = TwitterTestModule.class, injects = TimelineProviderTest.class)
    class LocalModule {
        @Provides @Singleton
        public Client provideClient() {
            return Mockito.mock(Client.class);
        }
    }

    @Before
    public void setUp() throws IOException {
        ObjectGraph.create(new LocalModule()).inject(this);
    }

    private static byte[] readFileToByte(String assetPath) throws IOException {
        if (assetPath == null) return new byte[0];

        AssetManager assetManager = Robolectric.application.getAssets();
        InputStream input = null;
        try {
            input = assetManager.open(assetPath);
            // File can't be more than 2 Go...
            byte[] lInputBuffer = new byte[input.available()];
            input.read(lInputBuffer);
            return lInputBuffer;
        } finally {
            if (input != null) input.close();
        }
    }

    private void given() throws IOException {
        when(client.execute(any(Request.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Request request = (Request) invocation.getArguments()[0];
                TypedInput response = new TypedByteArray("application/json", readFileToByte("json/timeline_01.json"));
                return new Response(request.getUrl(), 200, "", Collections.EMPTY_LIST, response);
            }
        });
    }

    @Test
    public void testTweet() throws IOException {
        dataSource.executeScriptFromAssets("sql/default.sql");
        given();
        from(timelineProvider.findTweets()).forEach(new Action1<TimelineViewModel>() {
            @Override
            public void call(TimelineViewModel timelineViewModel) {
                for (TweetItemViewModel tweetItemViewModel : timelineViewModel.tweetItems) {
                    System.out.println(tweetItemViewModel);
                }
            }
        });
    }

    @Test
    public void testTweet2() throws IOException {
        given();
        from(timelineProvider.findTweets()).forEach(new Action1<TimelineViewModel>() {
            @Override
            public void call(TimelineViewModel timelineViewModel) {
                for (TweetItemViewModel tweetItemViewModel : timelineViewModel.tweetItems) {
                    System.out.println(tweetItemViewModel);
                }
            }
        });
    }
}