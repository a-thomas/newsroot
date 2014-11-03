package com.codexperiments.newsroot.data.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.newsroot.core.domain.repository.AlreadyExistsException;
import com.codexperiments.newsroot.core.domain.repository.ChangedMeanwhileException;
import com.codexperiments.newsroot.core.domain.repository.DoesNotExistException;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.quickdao.sqlite.SQLiteQueryBuilder;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;
import static java.lang.String.format;
import static java.lang.String.valueOf;

public class SqliteTweetRepository implements TweetRepository {
    private SQLiteDatabase connection;

    public SqliteTweetRepository(SqliteTwitterDatabase database) {
        this.connection = database.getConnection();
    }

    protected boolean insert(Tweet tweet) throws AlreadyExistsException {
        if (tweet.getId() < 0) throw new IllegalArgumentException(format("Cannot insert tweet %s since it does not have a valid id", tweet));
        long tweetVersion = tweet.getVersion();
        if (tweetVersion != 0) throw new IllegalArgumentException(format("Cannot insert tweet %s since it has a version", tweet));

        String insert = "insert into TWT_TWEET(TWT_USR_ID, TWT_TEXT, TWT_CREATED_AT, TWT_VERSION, TWT_ID) values (?, ?, ?, ?, ?);";
        SQLiteStatement statement = connection.compileStatement(insert);
        statement.bindString(1, String.valueOf(tweet.getUser().getId()));
        statement.bindString(2, String.valueOf(tweet.getText()));
        statement.bindString(3, String.valueOf(tweet.getCreatedAt()));
        statement.bindString(4, String.valueOf(tweet.getVersion() + 1));
        statement.bindString(5, String.valueOf(tweet.getId()));

        try {
            long rowId = statement.executeInsert();
            //if (rowId > 0) {
            tweet.setVersion(tweetVersion + 1);
            return true;
            //}
        } catch (SQLiteException sqliteException) {
            throw new AlreadyExistsException("");
        } finally{
            statement.close();
        }
    }

    protected boolean update(Tweet tweet) throws ChangedMeanwhileException {
        if (tweet.getId() < 0) throw new IllegalArgumentException(format("Cannot update tweet %s since it does not have a valid id", tweet));
        long userVersion = tweet.getVersion();
        if (userVersion <= 0) throw new IllegalArgumentException(format("Cannot insert tweet %s since it does not have a version", tweet));

        String update = "update TWT_TWEET set TWT_USR_ID = ?, TWT_TEXT = ?, TWT_CREATED_AT = ?, TWT_VERSION = ? where TWT_ID = ? and TWT_VERSION = ?;";
        SQLiteStatement statement = connection.compileStatement(update);
        statement.bindString(1, String.valueOf(tweet.getUser().getId()));
        statement.bindString(2, String.valueOf(tweet.getText()));
        statement.bindString(3, String.valueOf(tweet.getCreatedAt()));
        statement.bindString(4, String.valueOf(tweet.getVersion() + 1));
        statement.bindString(5, String.valueOf(tweet.getId()));

        try {
            statement.bindString(6, String.valueOf(tweet.getVersion()));
            int updatedRowCount = statement.executeUpdateDelete();
            if (updatedRowCount == 1) {
                tweet.setVersion(userVersion + 1);
                return true;
            } else {
                throw new ChangedMeanwhileException("");
            }
        } finally{
            statement.close();
        }
    }

    @Override
    public void feed(Tweet tweet) {
        try {
            insert(tweet);
        } catch (AlreadyExistsException alreadyExistsException) {
            // Ignore
        }
    }

    @Override
    public void save(Tweet tweet) throws AlreadyExistsException, ChangedMeanwhileException {
        if (tweet.getVersion() == 0) {
            insert(tweet);
        } else {
            update(tweet);
        }
    }

    @Override
    public void delete(Tweet tweet) throws ChangedMeanwhileException {
        final String delete = "delete from TWT_TWEET where TWT_ID = ? and TWT_VERSION = ?;";

        SQLiteStatement statement = connection.compileStatement(delete);
        statement.bindString(1, String.valueOf(tweet.getId()));
        statement.bindString(2, String.valueOf(tweet.getVersion()));

        try {
            int deletedRows = statement.executeUpdateDelete();
            if (deletedRows != 1) throw new ChangedMeanwhileException("Object has not been deleted");
        } finally {
            statement.close();
        }
    }

    @Override
    public Tweet byId(long tweetId) throws DoesNotExistException {
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
            throw new DoesNotExistException(format("Tweet %s not found", tweetId));
        } finally {
            cursor.close();
        }
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
                tweet.setUser(userMapper.parseRow(cursor));
            }
            return tweet;
        }

        public void joinUser() {
            userMapper = new UserMapper();
        }
    }
}
