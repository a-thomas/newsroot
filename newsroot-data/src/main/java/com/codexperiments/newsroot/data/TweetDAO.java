package com.codexperiments.newsroot.data;

import android.database.Cursor;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.data.sqlite.TweetMapper;
import com.codexperiments.newsroot.data.sqlite.TweetTable;
import com.codexperiments.newsroot.data.sqlite.UserMapper;
import com.codexperiments.newsroot.data.sqlite.UserTable;
import com.codexperiments.quickdao.BaseQuery;
import com.codexperiments.quickdao.Database;
import com.codexperiments.quickdao.Query;

import static com.codexperiments.newsroot.data.sqlite.UserTable.USR_USER;

public class TweetDAO extends TweetTable {
    private TwitterDatabase database;

    public TweetDAO(TwitterDatabase database) {
        super(database.getConnection());
        this.database = database;
    }

    public FindAllTweet findAll() {
        return new FindAllTweet(database);
    }

    public static class FindAllTweet extends BaseQuery<Tweet, Mapper> {
        public FindAllTweet(Database database) {
            super(database, new Mapper(), new Query().from(TWT_TWEET).orderBy(TweetTable.TWT_ID).descending());
        }

        public FindAllTweet withUser() {
            mapper.joinUser();
            query.innerJoin(USR_USER).on(TweetTable.TWT_USR_ID, UserTable.USR_ID);
            return this;
        }

        public FindAllTweet pagedBy(int pPageSize) {
            query.limit(pPageSize);
            return this;
        }
    }

    private static class Mapper extends TweetMapper {
        private UserMapper userMapper;

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
