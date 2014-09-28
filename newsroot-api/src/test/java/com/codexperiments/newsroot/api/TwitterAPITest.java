package com.codexperiments.newsroot.api;

import com.codexperiments.newsroot.api.entity.Tweet;
import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.BlockingObservable;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TwitterAPITest {
    @Inject TwitterAPI api;

    @Module(complete = false, includes = TwitterTestModule.class, injects = TwitterAPITest.class)
    class TestModule {
    }

    @Before
    public void setUp() throws IOException {
        ObjectGraph.create(new TestModule()).inject(this);
    }

    @Test
    public void testFindTweet() {
        Observable<Tweet> homeTweets = api.findHomeTweets().flatMap(new Func1<List<Tweet>, Observable<Tweet>>() {
            public Observable<Tweet> call(List<Tweet> tweets) {
                return Observable.from(tweets);
            }
        });

        BlockingObservable.from(homeTweets).forEach(new Action1<Tweet>() {
            public void call(Tweet tweet) {
                System.out.println(tweet);
            }
        });
    }
}
