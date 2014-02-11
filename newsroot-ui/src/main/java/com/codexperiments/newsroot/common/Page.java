package com.codexperiments.newsroot.common;

public interface Page<TItem> /* extends Iterable<TItem> */{
    long oldestBound();

    long earliestBound();

    TItem get(int pIndex);

    int size();
}
