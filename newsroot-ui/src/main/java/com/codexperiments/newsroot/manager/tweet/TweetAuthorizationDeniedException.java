package com.codexperiments.newsroot.manager.tweet;

public class TweetAuthorizationDeniedException extends TweetAuthorizationFailedException {
    private static final long serialVersionUID = 9102531134652767782L;

    protected TweetAuthorizationDeniedException() {
        super("Authorization denied by user.");
    }

    public static TweetAuthorizationDeniedException authorizationDenied() {
        return new TweetAuthorizationDeniedException();
    }
}
