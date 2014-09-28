package com.codexperiments.newsroot.api;

import com.codexperiments.newsroot.api.entity.Tweet;
import retrofit.http.GET;
import rx.Observable;

import java.util.List;

public interface TwitterAPI {
    @GET("/1.1/statuses/home_timeline.json")
    Observable<List<Tweet>> findHomeTweets();
}
