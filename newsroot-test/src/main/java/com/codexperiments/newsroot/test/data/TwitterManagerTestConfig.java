package com.codexperiments.newsroot.test.data;

import com.codexperiments.newsroot.manager.twitter.TwitterManager;

public class TwitterManagerTestConfig implements TwitterManager.Config {
    public String getHost() {
        return "http://localhost:8378/";
    }

    public String getConsumerKey() {
        return "3Ng9QGTB7EpZCHDOIT2jg";
    }

    public String getConsumerSecret() {
        return "OolXzfWdSF6uMdgt2mvLNpDl4HOA1JNlN487LvDUA4";
    }

    public String getCallbackURL() {
        return "oauth://newsroot-callback";
    }
}
