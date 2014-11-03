package com.codexperiments.newsroot.data.sync;

import com.codexperiments.newsroot.api.TwitterAPI;
import com.codexperiments.newsroot.api.TwitterAPIModule;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.core.domain.repository.UserRepository;
import com.codexperiments.newsroot.core.provider.TimelineProvider;
import com.codexperiments.newsroot.core.service.TimelineSync;
import com.codexperiments.newsroot.data.provider.SqliteTimelineProvider;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterModule;
import com.codexperiments.newsroot.data.sync.assembler.TwitterAssembler;
import com.codexperiments.newsroot.data.sync.assembler.TwitterAssemblerImpl;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(complete = false, library = true, includes = {TwitterAPIModule.class, SqliteTwitterModule.class})
public class TwitterSyncModule {
    @Provides @Singleton
    public TwitterAssembler provideTwitterAssembler() {
        return new TwitterAssemblerImpl();
    }

    @Provides @Singleton
    public TimelineSync provideTweetSync(TwitterAPI twitterAPI,
                                      TweetRepository tweetRepository,
                                      UserRepository userRepository,
                                      TwitterAssembler twitterAssembler) {
        return new TwitterTimelineSync(twitterAPI, tweetRepository, userRepository, twitterAssembler);
    }

    @Provides
    public TimelineProvider provideTimelineProvider(SqliteTwitterDatabase datasource, TimelineSync timelineSync) {
        return new SqliteTimelineProvider(datasource, timelineSync);
    }
}
