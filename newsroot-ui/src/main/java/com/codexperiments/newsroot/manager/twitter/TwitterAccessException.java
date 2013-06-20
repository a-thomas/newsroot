package com.codexperiments.newsroot.manager.twitter;

public class TwitterAccessException extends Exception
{
    private static final long serialVersionUID = 9102531134652767782L;

    protected TwitterAccessException(Throwable pThrowable)
    {
        super("Twitter authorization failed.", pThrowable);
    }

    protected TwitterAccessException(String pMessage)
    {
        super(pMessage);
    }

    public static TwitterAccessException from(Throwable pThrowable)
    {
        return new TwitterAccessException(pThrowable);
    }
}
