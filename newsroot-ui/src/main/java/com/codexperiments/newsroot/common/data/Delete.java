package com.codexperiments.newsroot.common.data;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class Delete<TTable extends Enum<?> & Table> {
    private StringBuilder mDelete;
    private StringBuilder mWhere;
    private StringBuilder mOrderBy;

    private List<String> mParams;
    private SQLiteStatement mStatement;

    public static <TTable extends Enum<?> & Table> Delete<TTable> on(TTable pTable) {
        return new Delete<TTable>(pTable);
    }

    public Delete(TTable pTable) {
        super();

        mDelete = new StringBuilder("delete from ").append(pTable.name());
        mWhere = new StringBuilder();
        mOrderBy = new StringBuilder();

        mParams = new ArrayList<String>();
        mStatement = null;
    }

    private void startWhere() {
        if (mWhere.length() == 0) {
            mWhere.append(" where ");
        } else {
            mWhere.append(" and ");
        }
    }

    public Delete<TTable> whereEquals(Enum<?> pColumn, long pValue) {
        return whereCondition(pColumn, " = ", Long.toString(pValue));
    }

    public Delete<TTable> whereGreater(Enum<?> pColumn, long pValue) {
        return whereCondition(pColumn, " > ", Long.toString(pValue));
    }

    public Delete<TTable> whereLower(Enum<?> pColumn, long pValue) {
        return whereCondition(pColumn, " < ", Long.toString(pValue));
    }

    private Delete<TTable> whereCondition(Enum<?> pColumn, String pOperator, String pValue) {
        startWhere();
        mWhere.append(pColumn.name()).append(pOperator).append("?");
        mParams.add(pValue);
        return this;
    }

    public int execute(SQLiteDatabase pConnection) {
        if (mStatement == null) {
            StringBuilder lQuery = new StringBuilder();
            lQuery.append(mDelete).append(mWhere);
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
        return mStatement.executeUpdateDelete();
    }
}