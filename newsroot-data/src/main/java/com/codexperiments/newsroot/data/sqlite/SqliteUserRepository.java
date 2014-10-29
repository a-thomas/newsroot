package com.codexperiments.newsroot.data.sqlite;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.newsroot.core.domain.repository.UserRepository;
import com.codexperiments.quickdao.sqlite.SQLiteQueryBuilder;

import static com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase.*;
import static java.lang.String.valueOf;

public class SqliteUserRepository implements UserRepository {
    private SqliteTwitterDatabase database;
    private SQLiteDatabase connection;

    public SqliteUserRepository(SqliteTwitterDatabase database) {
        this.database = database;
        this.connection = database.getConnection();
    }

    @Override
    public void save(User user) {
        long userVersion = user.getVersion();
        boolean isNew = (userVersion == 0);

        SQLiteStatement statement;
        if (isNew) {
            final String insert = "insert into USR_USER(USR_NAME, USR_SCREEN_NAME, USR_VERSION, USR_ID) values (?, ?, ?, ?);";
            statement = connection.compileStatement(insert);
        } else {
            final String update = "update USR_USER set USR_NAME = ?, USR_SCREEN_NAME = ?, USR_VERSION = ? where USR_ID = ? and USR_VERSION = ?;";
            statement = connection.compileStatement(update);
        }

        statement.bindString(1, valueOf(user.getName()));
        statement.bindString(2, valueOf(user.getScreenName()));
        statement.bindString(3, valueOf(user.getVersion() + 1));
        statement.bindString(4, valueOf(user.getId()));

        if (isNew) {
            statement.executeInsert();
        } else {
            statement.bindString(5, valueOf(user.getVersion()));
            int updatedRowCount = statement.executeUpdateDelete();
            if (updatedRowCount != 1) {
                throw new RuntimeException("Object has not been updated");
            }
        }
        statement.close();
        user.setVersion(userVersion + 1);
    }

    @Override
    public void delete(User user) {
        final String delete = "delete from USR_USER where USR_ID = ?;";

        SQLiteStatement statement = connection.compileStatement(delete);
        statement.bindString(1, String.valueOf(user.getId()));

        int deletedRows = statement.executeUpdateDelete();
        if (deletedRows != 1) throw new RuntimeException("Object has not been deleted");
        statement.close();
    }

    @Override
    public User byId(long userId) {
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
            throw new RuntimeException("Entity not found " + userId);
        } finally {
            cursor.close();
        }
    }
}
