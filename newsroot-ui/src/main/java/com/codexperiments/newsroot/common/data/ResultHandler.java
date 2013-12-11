package com.codexperiments.newsroot.common.data;

import android.database.Cursor;

public class ResultHandler<TTable extends Enum<?> & Table> {
    public ResultHandler(TTable[] pTables) {
        super();
    }

    public ResultHandler<TTable> select(TTable pTable) {
        return this;
    }

    public void parse(Cursor pCursor, RowHandler pRowHandler) {
        // Iterate through the result set.
        boolean lHasNext = pCursor.moveToFirst();
        try {
            while (lHasNext) {
                pRowHandler.handleRow(pCursor);
                lHasNext = pCursor.moveToNext();
            }
        } finally {
            pCursor.close();
        }
    }
}