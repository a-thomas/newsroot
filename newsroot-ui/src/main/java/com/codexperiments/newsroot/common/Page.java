package com.codexperiments.newsroot.common;

public interface Page<TItem> extends Iterable<TItem> {
    int lowerBound();

    int upperBound();

    TItem get(int pIndex);

    int size();
}
