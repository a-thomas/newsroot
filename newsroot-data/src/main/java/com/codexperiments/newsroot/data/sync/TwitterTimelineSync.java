package com.codexperiments.newsroot.data.sync;

import com.codexperiments.newsroot.api.TwitterAPI;
import com.codexperiments.newsroot.api.entity.TweetDTO;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.core.domain.repository.UserRepository;
import com.codexperiments.newsroot.core.service.TimelineSync;
import com.codexperiments.newsroot.data.sync.assembler.TwitterAssembler;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.List;

public class TwitterTimelineSync implements TimelineSync {
    private final TwitterAPI twitterAPI;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;
    private final TwitterAssembler twitterAssembler;

    public TwitterTimelineSync(TwitterAPI twitterAPI, TweetRepository tweetRepository, UserRepository userRepository, TwitterAssembler twitterAssembler) {
        this.twitterAPI = twitterAPI;
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
        this.twitterAssembler = twitterAssembler;
    }

    @Override
    public Observable<List<Tweet>> synchronize() {
        return twitterAPI.findHomeTweets()
                         .observeOn(Schedulers.computation())
                         .map(new Func1<List<TweetDTO>, List<Tweet>>() {
                             @Override
                             public List<Tweet> call(List<TweetDTO> tweetDTOs) {
                                 return twitterAssembler.from(tweetDTOs);
                             }
                         })
                         .doOnNext(new Action1<List<Tweet>>() {
                             @Override
                             public void call(List<Tweet> tweetList) {
                                 Observable.from(tweetList).subscribe(new Action1<Tweet>() {
                                     @Override
                                     public void call(Tweet tweet) {
                                         userRepository.feed(tweet.getUser());
                                         tweetRepository.feed(tweet);
                                     }
                                 });
                             }
                         })
                         .finallyDo(new Action0() {
                             @Override
                             public void call() {
                                 // TODO Commit
                             }
                         });
    }
}
