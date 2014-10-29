package com.codexperiments.newsroot.data.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.quickdao.sqlite.SQLiteQueryBuilder;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;

public class SqliteTweetRepository implements TweetRepository {
    private SqliteTwitterDatabase database;
    private SQLiteDatabase connection;

    public SqliteTweetRepository(SqliteTwitterDatabase database) {
        this.database = database;
        this.connection = database.getConnection();
    }

    @Override
    public void save(Tweet tweet) {
        long tweetVersion = tweet.getVersion();
        boolean isNew = (tweetVersion == 0);

        SQLiteStatement statement;
        if (isNew) {
            final String insert = "insert into TWT_TWEET(TWT_USR_ID, TWT_TEXT, TWT_CREATED_AT, TWT_VERSION, TWT_ID) values (?, ?, ?, ?, ?);";
            statement = connection.compileStatement(insert);
        } else {
            final String update = "update TWT_TWEET set TWT_USR_ID = ?, TWT_TEXT = ?, TWT_CREATED_AT = ?, TWT_VERSION = ? where TWT_ID = ? and TWT_VERSION = ?;";
            statement = connection.compileStatement(update);
        }

        statement.bindString(1, String.valueOf(tweet.getUser().getId()));
        statement.bindString(2, String.valueOf(tweet.getText()));
        statement.bindString(3, String.valueOf(tweet.getCreatedAt()));
        statement.bindString(4, String.valueOf(tweet.getVersion() + 1));
        statement.bindString(5, String.valueOf(tweet.getId()));

        if (isNew) {
            statement.executeInsert();
        } else {
            statement.bindString(6, String.valueOf(tweet.getVersion()));
            int updatedRowCount = statement.executeUpdateDelete();
            if (updatedRowCount != 1) {
                throw new RuntimeException(String.format("Object has not been updated (%s lines)", updatedRowCount));
            }
        }
        statement.close();

        tweet.setVersion(tweetVersion + 1);
    }

    @Override
    public void delete(Tweet tweet) {
        final String delete = "delete from TWT_TWEET where TWT_ID = ?;";

        SQLiteStatement statement = connection.compileStatement(delete);
        statement.bindString(1, String.valueOf(tweet.getId()));

        int deletedRows = statement.executeUpdateDelete();
        if (deletedRows != 1) throw new RuntimeException("Object has not been deleted");
        statement.close();
    }

    @Override
    public Tweet byId(long tweetId) {
        Mapper mapper = new Mapper();
        mapper.joinUser();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder()
                .from(TWT_TWEET_TABLE, TWT_TWEET_COLUMNS)
                .innerJoin(USR_USER_TABLE, USR_USER_COLUMNS).on(TWT_USR_ID, USR_ID)
                .whereEquals(TWT_ID, tweetId)
                .orderBy(TWT_ID).descending();

        Cursor cursor = connection.rawQuery(queryBuilder.toQuery(), queryBuilder.toParams());
        try {
            mapper.initialize(cursor);
            int resultSize = cursor.getCount();
            for (int i = 0; i < resultSize; ++i) {
                cursor.moveToNext();
                return mapper.parseRow(cursor);
            }
            throw new RuntimeException("Entity not found " + tweetId);
        } finally {
            cursor.close();
        }
    }
//
//    @Override
//    public FindAllTweet findAll() {
//        return new FindAllTweet(database);
//    }
//
//    public static class FindAllTweet extends SQLiteQuery<Tweet, Mapper> implements TweetRepository.FindAllTweet {
////        final Database database;
////        final Mapper mapper;
////        final Query query;
//
//        public FindAllTweet(SQLiteDatasource datasource) {
//            super(datasource, new Mapper(), new SQLiteQueryBuilder()
//                    .from(TWT_TWEET_TABLE, TWT_TWEET_COLUMNS)
//                    .orderBy(TWT_ID).descending());
////            this.database = database;
////            this.mapper = new Mapper();
////            this.query = new Query().from(TWT_TWEET).orderBy(TweetTable.TWT_ID).descending();
//        }
//
//        public FindAllTweet withUser() {
//            entityMapper.joinUser();
//            queryBuilder.innerJoin(USR_USER_TABLE).on(TWT_USR_ID, USR_ID);
//            return this;
//        }
//
//        public FindAllTweet pagedBy(int pPageSize) {
//            queryBuilder.limit(pPageSize);
//            return this;
//        }
//    }
//
//    public List<Tweet> asList(TweetRepository.FindAllTweet query) {
//        return ((FindAllTweet) query).asList();
//    }

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
                tweet.setUser(userMapper.parseRow(cursor));
            }
            return tweet;
        }

        public void joinUser() {
            userMapper = new UserMapper();
        }
    }
}
