package com.codexperiments.newsroot.manager;

public class TwitterAuthorizationDeniedException extends TwitterAuthorizationFailedException
{
    private static final long serialVersionUID = 9102531134652767782L;

    protected TwitterAuthorizationDeniedException()
    {
        super("Authorization denied by user.");
    }

    public static TwitterAuthorizationDeniedException authorizationDenied()
    {
        return new TwitterAuthorizationDeniedException();
    }
}
