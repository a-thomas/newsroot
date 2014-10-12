package com.codexperiments.newsroot.data.sqlite;

import android.database.Cursor;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.quickdao.sqlite.SQLiteDatasource;
import com.codexperiments.quickdao.sqlite.SQLiteQuery;
import com.codexperiments.quickdao.sqlite.SQLiteQueryBuilder;

import java.util.List;

import static com.codexperiments.newsroot.data.sqlite.UserTable.USR_USER;

public class SqliteTweetRepository extends TweetTable implements TweetRepository {
    private SqliteTwitterDatabase database;

    public SqliteTweetRepository(SqliteTwitterDatabase database) {
        super(database.getConnection());
        this.database = database;
    }

    @Override
    public FindAllTweet findAll() {
        return new FindAllTweet(database);
    }

    public static class FindAllTweet extends SQLiteQuery<Tweet, Mapper> implements TweetRepository.FindAllTweet {
//        final Database database;
//        final Mapper mapper;
//        final Query query;

        public FindAllTweet(SQLiteDatasource datasource) {
            super(datasource, new Mapper(), new SQLiteQueryBuilder().from(TWT_TWEET).orderBy(TweetTable.TWT_ID).descending());
//            this.database = database;
//            this.mapper = new Mapper();
//            this.query = new Query().from(TWT_TWEET).orderBy(TweetTable.TWT_ID).descending();
        }

        public FindAllTweet withUser() {
            entityMapper.joinUser();
            queryBuilder.innerJoin(USR_USER).on(TweetTable.TWT_USR_ID, UserTable.USR_ID);
            return this;
        }

        public FindAllTweet pagedBy(int pPageSize) {
            queryBuilder.limit(pPageSize);
            return this;
        }
    }

    public List<Tweet> asList(TweetRepository.FindAllTweet query) {
        return ((FindAllTweet) query).asList();
    }

    public/*private*/ static class Mapper extends TweetMapper {
        public UserMapper userMapper;

        @Override
        public void initialize(Cursor cursor) {
            super.initialize(cursor);
            if (userMapper != null) userMapper.initialize(cursor);
        }

        @Override
        public Tweet parseRow(Cursor cursor) {
            Tweet tweet = super.parseRow(cursor);
            if (userMapper != null) {
                tweet.user = userMapper.parseRow(cursor);
            }
            return tweet;
        }

        public void joinUser() {
            userMapper = new UserMapper();
        }
    }
}
