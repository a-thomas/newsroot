package com.codexperiments.newsroot.data.tweet;

import android.database.Cursor;

import com.codexperiments.newsroot.common.data.RowHandler;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.COL_TWT_TWEET;
import com.codexperiments.newsroot.domain.tweet.Tweet;

public class TweetHandler implements RowHandler<Tweet> {
    private int mIdIndex;
    private int mNameIndex;
    private int mScreenNameIndex;
    private int mTextIndex;
    private int mCreatedAtIndex;

    public TweetHandler() {
        super();
    }

    @Override
    public void initialize(Cursor pCursor) {
        mIdIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_ID.name());
        mNameIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_NAME.name());
        mScreenNameIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_SCREEN_NAME.name());
        mTextIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_TEXT.name());
        mCreatedAtIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_CREATED_AT.name());
    }

    public TweetDTO parseDTO(Cursor pCursor) {
        TweetDTO lTweetDTO = new TweetDTO();
        lTweetDTO.setId(pCursor.getLong(mIdIndex));
        lTweetDTO.setName(pCursor.getString(mNameIndex));
        lTweetDTO.setScreenName(pCursor.getString(mScreenNameIndex));
        lTweetDTO.setText(pCursor.getString(mTextIndex));
        lTweetDTO.setCreatedAt(pCursor.getLong(mCreatedAtIndex));
        return lTweetDTO;
    }

    @Override
    public Tweet parse(Cursor pCursor) {
        Tweet lTweet = new Tweet();
        lTweet.setId(pCursor.getLong(mIdIndex));
        lTweet.setName(pCursor.getString(mNameIndex));
        lTweet.setScreenName(pCursor.getString(mScreenNameIndex));
        lTweet.setText(pCursor.getString(mTextIndex));
        lTweet.setCreatedAt(pCursor.getLong(mCreatedAtIndex));
        return lTweet;
    }

    public long getId(Cursor pCursor) {
        return pCursor.getLong(mIdIndex);
    }

    public String getName(Cursor pCursor) {
        return pCursor.getString(mNameIndex);
    }

    public String getScreenName(Cursor pCursor) {
        return pCursor.getString(mScreenNameIndex);
    }

    public String getText(Cursor pCursor) {
        return pCursor.getString(mTextIndex);
    }

    public long getCreatedAt(Cursor pCursor) {
        return pCursor.getLong(mCreatedAtIndex);
    }
}
