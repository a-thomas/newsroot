package com.codexperiments.newsroot.common.data;

import android.database.Cursor;

public interface ObjectHandler<TType> {
    void initialize(Cursor pCursor);

    TType parse(Cursor pCursor);
}
