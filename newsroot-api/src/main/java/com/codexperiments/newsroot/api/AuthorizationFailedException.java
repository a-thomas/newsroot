package com.codexperiments.newsroot.api;

import java.io.IOException;

public class AuthorizationFailedException extends IOException {
    private static final long serialVersionUID = 9102531134652767782L;

    protected AuthorizationFailedException(Throwable pThrowable) {
        super("Twitter authorization failed.", pThrowable);
    }

    protected AuthorizationFailedException(String pMessage) {
        super(pMessage);
    }

    public static AuthorizationFailedException authorizationFailed(Throwable throwable) {
        return new AuthorizationFailedException(throwable);
    }

    public static AuthorizationFailedException authorizationFailed(String message) {
        return new AuthorizationFailedException(message);
    }
}
