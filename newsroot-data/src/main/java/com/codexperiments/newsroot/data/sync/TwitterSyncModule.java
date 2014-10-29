package com.codexperiments.newsroot.data.sync;

import com.codexperiments.newsroot.api.TwitterAPI;
import com.codexperiments.newsroot.api.TwitterAPIModule;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.data.sync.assembler.TwitterAssembler;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = false, library = true, includes = {TwitterAPIModule.class})
public class TwitterSyncModule {
    @Provides
    @Singleton
    public TweetSync provideTweetRepository(TwitterAPI twitterAPI,
                                            TweetRepository tweetRepository,
                                            TwitterAssembler twitterAssembler) {
        return new TweetSync(twitterAPI, tweetRepository, twitterAssembler);
    }
}
