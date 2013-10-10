package com.codexperiments.newsroot.common;

public interface Page<TItem> /* extends Iterable<TItem> */{
    long lowerBound();

    long upperBound();

    TItem get(int pIndex);

    int size();
}
