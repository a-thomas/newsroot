package com.codexperiments.newsroot.data.sqlite;

import android.database.Cursor;
import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.quickdao.EntityMapper;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;

public class UserMapper
    implements EntityMapper<User> {
    protected int idIndex;
    protected int versionIndex;
    protected int nameIndex;
    protected int screenNameIndex;

    public Class<User> ofType() {
        return User.class;
    }

    public void initialize(Cursor cursor) {
        idIndex = cursor.getColumnIndex(USR_ID);
        versionIndex = cursor.getColumnIndex(USR_VERSION);
        nameIndex = cursor.getColumnIndex(USR_NAME);
        screenNameIndex = cursor.getColumnIndex(USR_SCREEN_NAME);
    }

    public User parseRow(Cursor cursor) {
        User user = new User();
        if (idIndex != -1) user.setId(cursor.getLong(idIndex));
        if (versionIndex != -1) user.setVersion(cursor.getLong(versionIndex));
        if (nameIndex != -1) user.setName(cursor.getString(nameIndex));
        if (screenNameIndex != -1) user.setScreenName(cursor.getString(screenNameIndex));
        return user;
    }

    public long getId(Cursor cursor) {
        return cursor.getLong(idIndex);
    }

    public String getName(Cursor cursor) {
        return cursor.getString(nameIndex);
    }

    public String getScreenName(Cursor cursor) {
        return cursor.getString(screenNameIndex);
    }
}
