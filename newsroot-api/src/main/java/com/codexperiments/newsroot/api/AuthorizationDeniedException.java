package com.codexperiments.newsroot.api;

import java.io.IOException;

public class AuthorizationDeniedException extends IOException {
    private static final long serialVersionUID = 9102531134652767782L;

    protected AuthorizationDeniedException() {
        super("Authorization denied by user.");
    }

    public static AuthorizationDeniedException authorizationDenied() {
        return new AuthorizationDeniedException();
    }
}
