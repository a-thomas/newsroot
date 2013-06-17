package com.codexperiments.newsroot.domain;

import java.util.List;

import com.codexperiments.newsroot.common.Id;

public class News {
    private Id mId;
    private List<Category> mCategory;
    private Source mSource;
    private Illustration mIllustration;
    private Title mTitle;
    private Content mContent;
}
