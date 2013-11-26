package com.codexperiments.newsroot.manager.tweet;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class Insert<TTable extends Enum<?> & Table> {
    private StringBuilder mInsert;
    private StringBuilder mValues;
    private StringBuilder mOrderBy;

    private List<String> mParams;
    private SQLiteStatement mStatement;

    public static <TTable extends Enum<?> & Table> Insert<TTable> on(TTable pTable) {
        return new Insert<TTable>(pTable);
    }

    public Insert(TTable pTable) {
        super();

        mInsert = new StringBuilder("insert into ").append(pTable.name());
        mValues = new StringBuilder();
        mOrderBy = new StringBuilder();

        mParams = new ArrayList<String>();
        mStatement = null;
    }

    private void startValueClause() {
        if (mValues.length() == 0) {
            mInsert.append(" (");
            mValues.append(") values (");
        } else {
            mInsert.append(", ");
            mValues.append(", ");
        }
    }

    public Insert<TTable> value(Enum<?> pColumn, String pValue) {
        if (mStatement == null) {
            startValueClause();
            mInsert.append(pColumn.name());
            mValues.append("?");
        }
        mParams.add(pValue);
        return this;
    }

    public Insert<TTable> value(Enum<?> pColumn, long pValue) {
        return value(pColumn, Long.toString(pValue));
    }

    public long execute(SQLiteDatabase pConnection) {
        if (mStatement == null) {
            StringBuilder lQuery = new StringBuilder();
            lQuery.append(mInsert).append(mValues).append(")");
            if (mOrderBy.length() > 0) {
                lQuery.append(mOrderBy);
            }

            mStatement = pConnection.compileStatement(lQuery.toString());
        }

        int lParamCount = mParams.size();
        for (int i = 0; i < lParamCount; ++i) {
            mStatement.bindString(i + 1, mParams.get(i));
        }
        mParams.clear();
        return mStatement.executeInsert();
    }
}