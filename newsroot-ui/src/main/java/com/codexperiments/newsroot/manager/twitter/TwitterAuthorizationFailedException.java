package com.codexperiments.newsroot.manager.twitter;

import android.net.Uri;

public class TwitterAuthorizationFailedException extends Exception
{
    private static final long serialVersionUID = 9102531134652767782L;

    protected TwitterAuthorizationFailedException(Throwable pThrowable)
    {
        super("Twitter authorization failed.", pThrowable);
    }

    protected TwitterAuthorizationFailedException(String pMessage)
    {
        super(pMessage);
    }

    public static IllegalArgumentException illegalCallbackUrl(Uri pUri)
    {
        return new IllegalArgumentException(String.format("Invalid twitter callback Url %1$s", pUri));
    }

    public static TwitterAuthorizationFailedException from(Throwable pThrowable)
    {
        return new TwitterAuthorizationFailedException(pThrowable);
    }
}
