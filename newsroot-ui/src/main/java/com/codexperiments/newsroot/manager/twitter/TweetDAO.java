package com.codexperiments.newsroot.manager.twitter;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.ResultHandler.Row;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.COL_TWT_TWEET;
import com.codexperiments.newsroot.manager.twitter.TwitterDatabase.DB_TWITTER;

public class TweetDAO {
    private TwitterDatabase mDatabase;
    ThreadLocal<Insert<DB_TWITTER>> mInsertHolder;
    ThreadLocal<Update<DB_TWITTER>> mUpdateHolder;

    public TweetDAO(TwitterDatabase pDatabase) {
        super();
        mDatabase = pDatabase;
        mInsertHolder = new ThreadLocal<Insert<DB_TWITTER>>();
        mUpdateHolder = new ThreadLocal<Update<DB_TWITTER>>();
    }

    public Tweet get(Row pRow) {
        Tweet lTweet = new Tweet();
        lTweet.setId(pRow.getLong(DB_TWITTER.TWT_TWEET, COL_TWT_TWEET.TWT_ID));
        lTweet.setName(pRow.getString(DB_TWITTER.TWT_TWEET, COL_TWT_TWEET.TWT_NAME));
        lTweet.setScreenName(pRow.getString(DB_TWITTER.TWT_TWEET, COL_TWT_TWEET.TWT_SCREEN_NAME));
        lTweet.setText(pRow.getString(DB_TWITTER.TWT_TWEET, COL_TWT_TWEET.TWT_TEXT));
        lTweet.setCreatedAt(pRow.getLong(DB_TWITTER.TWT_TWEET, COL_TWT_TWEET.TWT_CREATED_AT));
        return lTweet;
    }

    public void create(Tweet pTweet) {
        Insert<DB_TWITTER> lInsert = mInsertHolder.get();
        if (lInsert == null) {
            lInsert = Insert.on(DB_TWITTER.TWT_TWEET);
            mInsertHolder.set(lInsert);
        }
        lInsert.value(COL_TWT_TWEET.TWT_ID, pTweet.getId())
               .value(COL_TWT_TWEET.TWT_NAME, pTweet.getName())
               .value(COL_TWT_TWEET.TWT_SCREEN_NAME, pTweet.getScreenName())
               .value(COL_TWT_TWEET.TWT_TEXT, pTweet.getText())
               .value(COL_TWT_TWEET.TWT_CREATED_AT, pTweet.getCreatedAt())
               .execute(mDatabase.getWritableDatabase());
    }

    public void update(Tweet pTweet) {
        Update<DB_TWITTER> lUpdate = mUpdateHolder.get();
        if (lUpdate == null) {
            lUpdate = Update.on(DB_TWITTER.TWT_TWEET).whereEquals(COL_TWT_TWEET.TWT_ID, pTweet.getId());
            mUpdateHolder.set(lUpdate);
        }
        lUpdate.set(COL_TWT_TWEET.TWT_NAME, pTweet.getName())
               .set(COL_TWT_TWEET.TWT_SCREEN_NAME, pTweet.getScreenName())
               .set(COL_TWT_TWEET.TWT_TEXT, pTweet.getText())
               .set(COL_TWT_TWEET.TWT_CREATED_AT, pTweet.getCreatedAt())
               .execute(mDatabase.getWritableDatabase());
    }

    public void delete(TimeGap pTimeGap) {
        mDatabase.getWritableDatabase().execSQL("delete from TWT_TWEET where TWT_ID = ?",
                                                new String[] { Long.toString(pTimeGap.getId()) });
    }
}
