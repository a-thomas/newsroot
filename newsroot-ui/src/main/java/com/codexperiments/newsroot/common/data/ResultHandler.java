package com.codexperiments.newsroot.common.data;

import android.database.Cursor;
import android.inputmethodservice.Keyboard.Row;

public class ResultHandler<TTable extends Enum<?> & Table> {
    // private TTable[] mTables;
    // private boolean[] mSelectedTables;
    // private int[][] mIndexes;

    public ResultHandler(TTable[] pTables) {
        super();
        // mTables = pTables;
        // mSelectedTables = new boolean[pTables.length];
        // mIndexes = new int[mTables.length][];
    }

    public ResultHandler<TTable> select(TTable pTable) {
        // mSelectedTables[pTable.ordinal()] = true;
        return this;
    }

    public void parse(Cursor pCursor, Handle pHandle) {
        // Build the index of columns to look for in the result set.
        // int lTableCount = mTables.length;
        // for (int i = 0; i < lTableCount; ++i) {
        // if (mSelectedTables[i]) {
        // TTable lTable = mTables[i];
        //
        // Enum<?>[] lColumns = lTable.columns();
        // int lColumnCount = lColumns.length;
        //
        // mIndexes[i] = new int[lColumnCount];
        // for (int j = 0; j < lColumnCount; ++j) {
        // mIndexes[i][j] = pCursor.getColumnIndex(lColumns[j].name());
        // }
        // }
        // }

        // Iterate through the result set.
        Row lRow = new Row(pCursor, mIndexes);
        pCursor.moveToFirst();
        try {
            while (!pCursor.isAfterLast()) {
                pHandle.handleRow(lRow, pCursor);
                pCursor.moveToNext();
            }
        } finally {
            pCursor.close();
        }
    }

    // public static class Row {
    // private Cursor mCursor;
    // private int[][] mIndexes;
    //
    // public Row(Cursor pCursor, int[][] pIndexes) {
    // super();
    // mCursor = pCursor;
    // mIndexes = pIndexes;
    // }
    //
    // public int getIndex(Enum<?> pTable, Enum<?> pColumn) {
    // return mIndexes[pTable.ordinal()][pColumn.ordinal()];
    // }
    //
    // public int getInt(Enum<?> pTable, Enum<?> pColumn) {
    // return mCursor.getInt(mIndexes[pTable.ordinal()][pColumn.ordinal()]);
    // }
    //
    // public long getLong(Enum<?> pTable, Enum<?> pColumn) {
    // return mCursor.getLong(mIndexes[pTable.ordinal()][pColumn.ordinal()]);
    // }
    //
    // public String getString(Enum<?> pTable, Enum<?> pColumn) {
    // return mCursor.getString(mIndexes[pTable.ordinal()][pColumn.ordinal()]);
    // }
    // }

    public interface Handle {
        public abstract void handleRow(Row pRow, Cursor pCursor);
    }
}