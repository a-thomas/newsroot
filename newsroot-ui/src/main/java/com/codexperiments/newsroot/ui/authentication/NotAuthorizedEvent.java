package com.codexperiments.newsroot.ui.authentication;

import com.codexperiments.newsroot.api.AuthorizationDeniedException;

public class NotAuthorizedEvent
{
    public Throwable error;

    public NotAuthorizedEvent() {
        this.error = null;
    }

    public NotAuthorizedEvent(Throwable error) {
        this.error = error;
    }
}
