package com.codexperiments.newsroot.common.data;

import android.database.Cursor;

public interface RowHandler<TType> {
    void initialize(Cursor pCursor);

    TType parse(Cursor pCursor);
}
