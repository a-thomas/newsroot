package com.codexperiments.newsroot.api;

import java.io.IOException;

import static java.lang.String.format;

public class AuthorizationFailedException extends IOException {
    private static final long serialVersionUID = 9102531134652767782L;

    protected AuthorizationFailedException(Throwable pThrowable) {
        super("Twitter authorization failed.", pThrowable);
    }

    protected AuthorizationFailedException(String message, Object... parameters) {
        super(format(message, parameters));
    }

    public static AuthorizationFailedException authorizationFailed(Throwable throwable) {
        return new AuthorizationFailedException(throwable);
    }

    public static AuthorizationFailedException authorizationFailed(String message, Object... parameters) {
        return new AuthorizationFailedException(format(message, parameters));
    }
}
