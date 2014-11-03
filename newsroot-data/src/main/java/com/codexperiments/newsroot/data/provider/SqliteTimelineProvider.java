package com.codexperiments.newsroot.data.provider;

import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.provider.TimelineProvider;
import com.codexperiments.newsroot.core.provider.TimelineViewModel;
import com.codexperiments.newsroot.core.provider.TweetItemViewModel;
import com.codexperiments.newsroot.core.service.TimelineSync;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import com.codexperiments.quickdao.sqlite.SQLiteCursorList;
import com.codexperiments.quickdao.sqlite.SQLiteQuery;
import com.codexperiments.quickdao.sqlite.SQLiteQueryBuilder;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.List;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;
import static rx.Observable.just;

public class SqliteTimelineProvider implements TimelineProvider {
    private SqliteTwitterDatabase dataSource;
    private TimelineSync timelineSync;

    public SqliteTimelineProvider(SqliteTwitterDatabase dataSource, TimelineSync timelineSync) {
        this.dataSource = dataSource;
        this.timelineSync = timelineSync;
    }

    public Observable<TimelineViewModel> findTweets() {
        return tweetsFromCache()
                .flatMap(new Func1<TimelineViewModel, Observable<TimelineViewModel>>() {
                    @Override
                    public Observable<TimelineViewModel> call(TimelineViewModel cachedTweets) {
                        if (!cachedTweets.tweetItems.isEmpty()) return just(cachedTweets);
                        else return remoteTweets();
                    }
                });
    }

    public Observable<TimelineViewModel> tweetsFromCache() {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder().from(TWT_TWEET_TABLE, TWT_TWEET_COLUMNS)
                                                                  .innerJoin(USR_USER_TABLE, USR_USER_COLUMNS)
                                                                  .on(TWT_USR_ID, USR_ID)
                                                                  .orderBy(TWT_ID).descending();
        return new SQLiteQuery<>(dataSource, new TweetItemViewModelMapper(), queryBuilder)
                .asObservableList()
                .subscribeOn(Schedulers.computation())
                .map(new Func1<SQLiteCursorList<TweetItemViewModel>, TimelineViewModel>() {
                    @Override
                    public TimelineViewModel call(SQLiteCursorList<TweetItemViewModel> tweetItemViewModels) {
                        return new TimelineViewModel(tweetItemViewModels);
                    }
                });
    }

    public Observable<TimelineViewModel> remoteTweets() {
        return timelineSync.synchronize().flatMap(new Func1<List<Tweet>, Observable<TimelineViewModel>>() {
            @Override
            public Observable<TimelineViewModel> call(List<Tweet> empty) {
                return tweetsFromCache();
            }
        });
    }
}
