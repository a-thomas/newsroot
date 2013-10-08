package com.codexperiments.newsroot.common.structure;

import java.util.List;

import com.codexperiments.newsroot.common.Page;

public interface PageIndex<TItem> {
    public List<TItem> find(int pIntervalIndex, int pIntervalSize);

    public void insert(Page<? extends TItem> pPage);

    public int size();
}
