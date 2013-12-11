package com.codexperiments.newsroot.data.tweet;

import android.database.Cursor;

import com.codexperiments.newsroot.common.data.ObjectHandler;
import com.codexperiments.newsroot.data.tweet.TweetDatabase.COL_TWT_TWEET;

public class TweetHandler implements ObjectHandler<TweetDTO> {
    private int mIdIndex;
    private int mNameIndex;
    private int mScreenNameIndex;
    private int mTextIndex;
    private int mCreatedAtIndex;

    public TweetHandler() {
        super();
    }

    @Override
    public Class<TweetDTO> ofType() {
        return TweetDTO.class;
    }

    @Override
    public void initialize(Cursor pCursor) {
        mIdIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_ID.name());
        mNameIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_NAME.name());
        mScreenNameIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_SCREEN_NAME.name());
        mTextIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_TEXT.name());
        mCreatedAtIndex = pCursor.getColumnIndex(COL_TWT_TWEET.TWT_CREATED_AT.name());
    }

    @Override
    public TweetDTO parse(Cursor pCursor) {
        TweetDTO lTweetDTO = new TweetDTO();
        lTweetDTO.setId(pCursor.getLong(mIdIndex));
        lTweetDTO.setName(pCursor.getString(mNameIndex));
        lTweetDTO.setScreenName(pCursor.getString(mScreenNameIndex));
        lTweetDTO.setText(pCursor.getString(mTextIndex));
        lTweetDTO.setCreatedAt(pCursor.getLong(mCreatedAtIndex));
        return lTweetDTO;
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
