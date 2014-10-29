package com.codexperiments.newsroot.data.sync;

import com.codexperiments.newsroot.api.TwitterAPI;
import com.codexperiments.newsroot.api.entity.TweetDTO;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.data.sync.assembler.TwitterAssembler;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.List;

public class TweetSync {
    private final TwitterAPI twitterAPI;
    private final TweetRepository tweetRepository;
    private final TwitterAssembler twitterAssembler;

    public TweetSync(TwitterAPI twitterAPI, TweetRepository tweetRepository, TwitterAssembler twitterAssembler) {
        this.twitterAPI = twitterAPI;
        this.tweetRepository = tweetRepository;
        this.twitterAssembler = twitterAssembler;
    }

    public void synchronize() {
        twitterAPI.findHomeTweets().observeOn(Schedulers.computation())
                  .flatMap(new Func1<List<TweetDTO>, Observable<TweetDTO>>() {
                      public Observable<TweetDTO> call(List<TweetDTO> tweets) {
                          return Observable.from(tweets);
                      }
                  })
                  .map(new Func1<TweetDTO, Tweet>() {
                      @Override
                      public Tweet call(TweetDTO tweetDTO) {
                          return twitterAssembler.from(tweetDTO);
                      }
                  })
                  .subscribe(new Observer<Tweet>() {
                      @Override
                      public void onNext(Tweet tweet) {
                          tweetRepository.save(tweet);
                      }

                      @Override
                      public void onCompleted() {
                          // TODO Commit gap
                      }

                      @Override
                      public void onError(Throwable e) {
                          // TODO
                      }
                  });
    }
}
