package com.codexperiments.newsroot.manager.twitter;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.codexperiments.newsroot.manager.twitter.ResultHandler.Handle;

public class Query<TTable extends Enum<?> & Table>
{
    private StringBuilder mSelect;
    private StringBuilder mFrom;
    private StringBuilder mWhere;
    private StringBuilder mOrderBy;

    private boolean mAscending;
    private boolean mDistinct;
    private int mLimit;

    private List<String> mParams;
    private ResultHandler<TTable> mResultHandler;

    public static <TTable extends Enum<?> & Table> Query<TTable> on(TTable[] pTables)
    {
        return new Query<TTable>(pTables);
    }

    public Query(TTable[] pTables)
    {
        super();

        mSelect = new StringBuilder();
        mFrom = new StringBuilder();
        mWhere = new StringBuilder();
        mOrderBy = new StringBuilder();

        mAscending = true;
        mDistinct = false;
        mLimit = 0;

        mParams = new ArrayList<String>();
        mResultHandler = new ResultHandler<TTable>(pTables);
    }

    public Query<TTable> limit(int pCount)
    {
        mLimit = pCount;
        return this;
    }

    private void startSelect()
    {
        if (mSelect.length() == 0) {
            mSelect.append("select ");
            if (mDistinct) {
                mSelect.append("distinct ");
            }
        } else {
            mSelect.append(", ");
        }
    }

    public Query<TTable> selectAll(TTable pTable)
    {
        startSelect();
        mSelect.append(pTable.name()).append(".*");
        mResultHandler.select(pTable);
        return this;
    }

    private void startFrom()
    {
        if (mFrom.length() == 0) {
            mFrom.append(" from ");
        } else {
            mFrom.append(", ");
        }
    }

    public Query<TTable> from(TTable pTable)
    {
        startFrom();
        mFrom.append(pTable.name());
        return this;
    }

    private void startWhere()
    {
        if (mWhere.length() == 0) {
            mWhere.append(" where ");
        } else {
            mWhere.append(" and ");
        }
    }

    public Query<TTable> whereEquals(Enum<?> pColumn, long pValue)
    {
        return whereCondition(pColumn, " = ", Long.toString(pValue));
    }

    public Query<TTable> whereGreater(Enum<?> pColumn, long pValue)
    {
        return whereCondition(pColumn, " > ", Long.toString(pValue));
    }

    public Query<TTable> whereLower(Enum<?> pColumn, long pValue)
    {
        return whereCondition(pColumn, " < ", Long.toString(pValue));
    }

    private Query<TTable> whereCondition(Enum<?> pColumn, String pOperator, String pValue)
    {
        startWhere();
        mWhere.append(pColumn.name()).append(pOperator).append("?");
        mParams.add(pValue);
        return this;
    }

    private void startOrderByClause()
    {
        if (mOrderBy.length() == 0) {
            mOrderBy.append(" order by ");
        } else {
            mOrderBy.append(", ");
        }
    }

    public Query<TTable> orderBy(Enum<?> pColumn)
    {
        startOrderByClause();
        mOrderBy.append(pColumn);
        mOrderBy.append(" ");
        return this;
    }

    public Query<TTable> ascending()
    {
        this.mAscending = true;
        return this;
    }

    public Query<TTable> descending()
    {
        this.mAscending = false;
        return this;
    }

    public void execute(SQLiteDatabase pConnection, Handle pHandle)
    {
        String[] lParams = mParams.toArray(new String[mParams.size()]);
        StringBuilder lQuery = new StringBuilder();
        lQuery.append(mSelect).append(mFrom).append(mWhere);
        if (mOrderBy.length() > 0) {
            lQuery.append(mOrderBy);
            if (mAscending) {
                lQuery.append(" asc");
            } else {
                lQuery.append(" desc");
            }
        }
        if (mLimit > 0) {
            lQuery.append(" limit ").append(Integer.toString(mLimit));
        }

        Cursor lCursor = pConnection.rawQuery(lQuery.toString(), lParams);
        mResultHandler.parse(lCursor, pHandle);
    }
}