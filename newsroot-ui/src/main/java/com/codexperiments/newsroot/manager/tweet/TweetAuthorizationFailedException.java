package com.codexperiments.newsroot.manager.tweet;

import android.net.Uri;

public class TweetAuthorizationFailedException extends Exception {
    private static final long serialVersionUID = 9102531134652767782L;

    protected TweetAuthorizationFailedException(Throwable pThrowable) {
        super("Twitter authorization failed.", pThrowable);
    }

    protected TweetAuthorizationFailedException(String pMessage) {
        super(pMessage);
    }

    public static IllegalArgumentException illegalCallbackUrl(Uri pUri) {
        return new IllegalArgumentException(String.format("Invalid twitter callback Url %1$s", pUri));
    }

    public static TweetAuthorizationFailedException from(Throwable pThrowable) {
        return new TweetAuthorizationFailedException(pThrowable);
    }
}
