package com.codexperiments.newsroot.data.provider;

import android.database.Cursor;
import com.codexperiments.newsroot.core.provider.TweetItemViewModel;
import com.codexperiments.quickdao.EntityMapper;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;

public class TweetItemViewModelMapper implements EntityMapper<TweetItemViewModel> {
    protected int tweetIdIndex;
    protected int tweetVersionIndex;
    protected int tweetTextIndex;
    protected int tweetCreatedAtIndex;
    protected int userIdIndex;
    protected int userVersionIndex;
    protected int userNameIndex;
    protected int userScreenNameIndex;

    public Class<TweetItemViewModel> ofType() {
        return TweetItemViewModel.class;
    }

    public void initialize(Cursor cursor) {
        tweetIdIndex = cursor.getColumnIndex(TWT_ID);
        tweetVersionIndex = cursor.getColumnIndex(TWT_VERSION);
        tweetTextIndex = cursor.getColumnIndex(TWT_TEXT);
        tweetCreatedAtIndex = cursor.getColumnIndex(TWT_CREATED_AT);
        userIdIndex = cursor.getColumnIndex(USR_ID);
        userVersionIndex = cursor.getColumnIndex(USR_VERSION);
        userNameIndex = cursor.getColumnIndex(USR_NAME);
        userScreenNameIndex = cursor.getColumnIndex(USR_SCREEN_NAME);
    }

    public TweetItemViewModel parseRow(Cursor cursor) {
        TweetItemViewModel tweetItemViewModel = new TweetItemViewModel();
        tweetItemViewModel.tweetId = cursor.getLong(tweetIdIndex);
        tweetItemViewModel.tweetVersion = cursor.getLong(tweetVersionIndex);
        tweetItemViewModel.tweetText = cursor.getString(tweetTextIndex);
        tweetItemViewModel.tweetCreatedAt = cursor.getLong(tweetCreatedAtIndex);
        tweetItemViewModel.userId = cursor.getLong(userIdIndex);
        tweetItemViewModel.userVersion = cursor.getLong(userVersionIndex);
        tweetItemViewModel.userName = cursor.getString(userNameIndex);
        tweetItemViewModel.userScreenName = cursor.getString(userScreenNameIndex);
        return tweetItemViewModel;
    }
}
