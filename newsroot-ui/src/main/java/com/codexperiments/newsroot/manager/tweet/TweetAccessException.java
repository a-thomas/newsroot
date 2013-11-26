package com.codexperiments.newsroot.manager.tweet;

public class TweetAccessException extends Exception {
    private static final long serialVersionUID = 9102531134652767782L;

    protected TweetAccessException(Throwable pThrowable) {
        super("Twitter access failed.", pThrowable);
    }

    protected TweetAccessException(String pMessage, Throwable pThrowable) {
        super(pMessage, pThrowable);
    }

    public static TweetAccessException from(Throwable pThrowable) {
        return new TweetAccessException(pThrowable);
    }

    public static TweetAccessException internalError(Throwable pThrowable) {
        return new TweetAccessException("Internal error", pThrowable);
    }
}
