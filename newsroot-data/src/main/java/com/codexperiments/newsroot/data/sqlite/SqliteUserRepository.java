package com.codexperiments.newsroot.data.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.newsroot.core.domain.repository.AlreadyExistsException;
import com.codexperiments.newsroot.core.domain.repository.ChangedMeanwhileException;
import com.codexperiments.newsroot.core.domain.repository.DoesNotExistException;
import com.codexperiments.newsroot.core.domain.repository.UserRepository;
import com.codexperiments.quickdao.sqlite.SQLiteQueryBuilder;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;
import static java.lang.String.format;
import static java.lang.String.valueOf;

// TODO Insert if not exist with cache
public class SqliteUserRepository implements UserRepository {
    private SqliteTwitterDatabase database;
    private SQLiteDatabase connection;

    public SqliteUserRepository(SqliteTwitterDatabase database) {
        this.database = database;
        this.connection = database.getConnection();
    }

    protected boolean insert(User user) throws AlreadyExistsException {
        if (user.getId() < 0) throw new IllegalArgumentException(format("Cannot insert user %s since it does not have a valid id", user));
        long userVersion = user.getVersion();
        if (userVersion != 0) throw new IllegalArgumentException(format("Cannot insert user %s since it has a version", user));

        String insert = "insert into USR_USER(USR_NAME, USR_SCREEN_NAME, USR_VERSION, USR_ID) values (?, ?, ?, ?);";
        SQLiteStatement statement = connection.compileStatement(insert);
        statement.bindString(1, valueOf(user.getName()));
        statement.bindString(2, valueOf(user.getScreenName()));
        statement.bindString(3, valueOf(user.getVersion() + 1));
        statement.bindString(4, valueOf(user.getId()));

        try {
            long rowId = statement.executeInsert();
            //if (rowId > 0) {
                user.setVersion(userVersion + 1);
                return true;
            //}
        } catch (SQLiteException sqliteException) {
            throw new AlreadyExistsException("");
        } finally{
            statement.close();
        }
    }

    protected boolean update(User user) throws ChangedMeanwhileException {
        if (user.getId() < 0) throw new IllegalArgumentException(format("Cannot update user %s since it does not have a valid id", user));
        long userVersion = user.getVersion();
        if (userVersion <= 0) throw new IllegalArgumentException(format("Cannot insert user %s since it does not have a version", user));

        String update = "update USR_USER set USR_NAME = ?, USR_SCREEN_NAME = ?, USR_VERSION = ? where USR_ID = ? and USR_VERSION = ?;";
        SQLiteStatement statement = connection.compileStatement(update);
        statement.bindString(1, valueOf(user.getName()));
        statement.bindString(2, valueOf(user.getScreenName()));
        statement.bindString(3, valueOf(user.getVersion() + 1));
        statement.bindString(4, valueOf(user.getId()));

        try {
            statement.bindString(5, valueOf(user.getVersion()));
            int updatedRowCount = statement.executeUpdateDelete();
            if (updatedRowCount == 1) {
                user.setVersion(userVersion + 1);
                return true;
            } else {
                throw new ChangedMeanwhileException("");
            }
        } finally{
            statement.close();
        }
    }

    @Override
    public void feed(User user) {
        try {
            insert(user);
        } catch (AlreadyExistsException alreadyExistsException) {
            // Ignore
        }
    }

    @Override
    public void save(User user) throws AlreadyExistsException, ChangedMeanwhileException {
        if (user.getVersion() == 0) {
            insert(user);
        } else {
            update(user);
        }
    }

    @Override
    public void delete(User user) throws ChangedMeanwhileException {
        final String delete = "delete from USR_USER where USR_ID = ? and USR_VERSION = ?;";

        SQLiteStatement statement = connection.compileStatement(delete);
        statement.bindString(1, String.valueOf(user.getId()));
        statement.bindString(2, String.valueOf(user.getVersion()));

        try {
            int deletedRows = statement.executeUpdateDelete();
            if (deletedRows != 1) throw new ChangedMeanwhileException("Object has not been deleted");
        } finally {
            statement.close();
        }
    }

    @Override
    public User byId(long userId) throws DoesNotExistException {
        UserMapper mapper = new UserMapper();

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder()
                .from(USR_USER_TABLE, USR_USER_COLUMNS)
                .whereEquals(USR_ID, userId);

        Cursor cursor = connection.rawQuery(queryBuilder.toQuery(), queryBuilder.toParams());
        try {
            mapper.initialize(cursor);
            int resultSize = cursor.getCount();
            for (int i = 0; i < resultSize; ++i) {
                cursor.moveToNext();
                return mapper.parseRow(cursor);
            }
            throw new DoesNotExistException(format("User %s not found", userId));
        } finally {
            cursor.close();
        }
    }
}
