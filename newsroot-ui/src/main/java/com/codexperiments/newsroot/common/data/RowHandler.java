package com.codexperiments.newsroot.common.data;

import android.database.Cursor;

public interface RowHandler {
    public abstract void handleRow(Cursor pCursor);
}
