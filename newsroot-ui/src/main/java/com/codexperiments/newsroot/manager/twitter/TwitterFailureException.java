package com.codexperiments.newsroot.manager.twitter;

public class TwitterFailureException extends Exception
{
    private static final long serialVersionUID = 9102531134652767782L;

    protected TwitterFailureException(Throwable pThrowable)
    {
        super("Twitter authorization failed.", pThrowable);
    }

    public static TwitterFailureException authorizationFailed(Throwable pThrowable)
    {
        return new TwitterFailureException(pThrowable);
    }
}
