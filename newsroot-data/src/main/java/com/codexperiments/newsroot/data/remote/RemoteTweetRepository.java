package com.codexperiments.newsroot.data.remote;

import android.database.Cursor;
import com.codexperiments.newsroot.api.TwitterAPI;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.data.sqlite.*;
import com.codexperiments.quickdao.Query;
import com.codexperiments.quickdao.sqlite.SQLiteDatasource;
import com.codexperiments.quickdao.sqlite.SQLiteQuery;
import com.codexperiments.quickdao.sqlite.SQLiteQueryBuilder;
import rx.functions.Func1;

import java.util.List;

import static com.codexperiments.newsroot.data.sqlite.UserTable.USR_USER;

public class RemoteTweetRepository implements TweetRepository {
    private TwitterAPI twitterAPI;

    public RemoteTweetRepository(TwitterAPI twitterAPI) {
        this.twitterAPI = twitterAPI;
    }

    @Override
    public FindAllTweet findAll() {
        return new FindAllTweet();
    }

    public static class FindAllTweet implements TweetRepository.FindAllTweet {
        public FindAllTweet() {
        }

        public FindAllTweet withUser() {
            return this;
        }

        public FindAllTweet pagedBy(int pPageSize) {
            return this;
        }

        @Override
        public <TResult> TResult retrieve(Func1<Query<Tweet>, TResult> retriever) {
            return null;
        }
    }
}
