package com.codexperiments.newsroot.ui.fragment;

import com.codexperiments.newsroot.common.event.BaseEvent;
import com.codexperiments.newsroot.common.event.EventListener;
import com.codexperiments.newsroot.manager.TwitterAuthorizationDeniedException;

public class UnauthorizedEvent extends BaseEvent<UnauthorizedEvent.Listener>
{
    private Throwable mException;

    protected UnauthorizedEvent()
    {
        super();
        mException = null;
    }

    protected UnauthorizedEvent(Throwable pException)
    {
        super();
        mException = pException;
    }

    @Override
    protected void notify(Listener pListener)
    {
        if ((mException instanceof TwitterAuthorizationDeniedException)) {
            pListener.onAuthorizationDenied();
        } else {
            pListener.onAuthorizationFailed();
        }
    }


    public interface Listener extends EventListener
    {
        void onAuthorizationFailed();

        void onAuthorizationDenied();
    }
}
