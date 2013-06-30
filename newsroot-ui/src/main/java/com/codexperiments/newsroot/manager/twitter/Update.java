package com.codexperiments.newsroot.manager.twitter;

import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class Update<TTable extends Enum<?> & Table> {
    private StringBuilder mUpdate;
    private StringBuilder mSet;
    private StringBuilder mWhere;
    private StringBuilder mOrderBy;

    private List<String> mParams;
    private SQLiteStatement mStatement;

    public static <TTable extends Enum<?> & Table> Update<TTable> on(TTable pTable) {
        return new Update<TTable>(pTable);
    }

    public Update(TTable pTable) {
        super();

        mUpdate = new StringBuilder("update ").append(pTable.name());
        mSet = new StringBuilder();
        mWhere = new StringBuilder();
        mOrderBy = new StringBuilder();

        mParams = new ArrayList<String>();
        mStatement = null;
    }

    private void startSetClause() {
        if (mSet.length() == 0) {
            mSet.append(" set ");
        } else {
            mSet.append(", ");
        }
    }

    public Update<TTable> set(Enum<?> pColumn, String pValue) {
        if (mStatement == null) {
            startSetClause();
            mSet.append(pColumn.name()).append(" = ? ");
        }
        mParams.add(pValue);
        return this;
    }

    public Update<TTable> set(Enum<?> pColumn, long pValue) {
        return set(pColumn, Long.toString(pValue));
    }

    private void startWhere() {
        if (mWhere.length() == 0) {
            mWhere.append(" where ");
        } else {
            mWhere.append(" and ");
        }
    }

    public Update<TTable> whereEquals(Enum<?> pColumn, long pValue) {
        return whereCondition(pColumn, " = ", Long.toString(pValue));
    }

    public Update<TTable> whereGreater(Enum<?> pColumn, long pValue) {
        return whereCondition(pColumn, " > ", Long.toString(pValue));
    }

    public Update<TTable> whereLower(Enum<?> pColumn, long pValue) {
        return whereCondition(pColumn, " < ", Long.toString(pValue));
    }

    private Update<TTable> whereCondition(Enum<?> pColumn, String pOperator, String pValue) {
        startWhere();
        mWhere.append(pColumn.name()).append(pOperator).append("?");
        mParams.add(pValue);
        return this;
    }

    public int execute(SQLiteDatabase pConnection) {
        if (mStatement == null) {
            StringBuilder lQuery = new StringBuilder();
            lQuery.append(mUpdate).append(mSet).append(mWhere);
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