package com.codexperiments.newsroot.data.tweet;

import com.codexperiments.newsroot.common.data.Insert;
import com.codexperiments.newsroot.common.data.ResultHandler;
import com.codexperiments.newsroot.common.data.Update;
import com.codexperiments.newsroot.common.data.ResultHandler.Row;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.COL_TWT_TWEET;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.DB_TWEET;
import com.codexperiments.newsroot.domain.tweet.TimeGap;
import com.codexperiments.newsroot.domain.tweet.Tweet;

public class TweetDAO {
    private TweetDatabase mDatabase;
    ThreadLocal<Insert<DB_TWEET>> mInsertHolder;
    ThreadLocal<Update<DB_TWEET>> mUpdateHolder;

    public TweetDAO(TweetDatabase pDatabase) {
        super();
        mDatabase = pDatabase;
        mInsertHolder = new ThreadLocal<Insert<DB_TWEET>>();
        mUpdateHolder = new ThreadLocal<Update<DB_TWEET>>();
    }

    public Tweet get(Row pRow) {
        Tweet lTweet = new Tweet();
        lTweet.setId(pRow.getLong(DB_TWEET.TWT_TWEET, COL_TWT_TWEET.TWT_ID));
        lTweet.setName(pRow.getString(DB_TWEET.TWT_TWEET, COL_TWT_TWEET.TWT_NAME));
        lTweet.setScreenName(pRow.getString(DB_TWEET.TWT_TWEET, COL_TWT_TWEET.TWT_SCREEN_NAME));
        lTweet.setText(pRow.getString(DB_TWEET.TWT_TWEET, COL_TWT_TWEET.TWT_TEXT));
        lTweet.setCreatedAt(pRow.getLong(DB_TWEET.TWT_TWEET, COL_TWT_TWEET.TWT_CREATED_AT));
        return lTweet;
    }

    public void create(Tweet pTweet) {
        Insert<DB_TWEET> lInsert = mInsertHolder.get();
        if (lInsert == null) {
            lInsert = Insert.on(DB_TWEET.TWT_TWEET);
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
        Update<DB_TWEET> lUpdate = mUpdateHolder.get();
        if (lUpdate == null) {
            lUpdate = Update.on(DB_TWEET.TWT_TWEET).whereEquals(COL_TWT_TWEET.TWT_ID, pTweet.getId());
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
