package com.codexperiments.newsroot.data.sqlite;

import android.database.Cursor;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.quickdao.EntityMapper;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;

public class TweetMapper
    implements EntityMapper<Tweet> {
    protected int idIndex;
    protected int versionIndex;
    protected int textIndex;
    protected int createdAtIndex;

    public Class<Tweet> ofType() {
        return Tweet.class;
    }

    public void initialize(Cursor cursor) {
        idIndex = cursor.getColumnIndex(TWT_ID);
        versionIndex = cursor.getColumnIndex(TWT_VERSION);
        textIndex = cursor.getColumnIndex(TWT_TEXT);
        createdAtIndex = cursor.getColumnIndex(TWT_CREATED_AT);
    }

    public Tweet parseRow(Cursor cursor) {
        Tweet tweet = new Tweet();
        if (idIndex != -1) tweet.setId(cursor.getLong(idIndex));
        if (versionIndex != -1) tweet.setVersion(cursor.getLong(versionIndex));
        if (textIndex != -1) tweet.setText(cursor.getString(textIndex));
        if (createdAtIndex != -1) tweet.setCreatedAt(cursor.getLong(createdAtIndex));
        return tweet;
    }

    public long getId(Cursor cursor) {
        return cursor.getLong(idIndex);
    }

    public long getVersion(Cursor cursor) {
        return cursor.getLong(versionIndex);
    }

    public String getText(Cursor cursor) {
        return cursor.getString(textIndex);
    }

    public long getCreatedAt(Cursor cursor) {
        return cursor.getLong(createdAtIndex);
    }
}
