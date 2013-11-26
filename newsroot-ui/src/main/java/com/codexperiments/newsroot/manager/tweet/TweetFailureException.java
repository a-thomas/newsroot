package com.codexperiments.newsroot.manager.tweet;

public class TweetFailureException extends Exception {
    private static final long serialVersionUID = 9102531134652767782L;

    protected TweetFailureException(Throwable pThrowable) {
        super("Twitter failure.", pThrowable);
    }

    protected TweetFailureException(String pMessage, Throwable pThrowable) {
        super(pMessage, pThrowable);
    }

    public static TweetFailureException authorizationFailed(Throwable pThrowable) {
        return new TweetFailureException(pThrowable);
    }
}
