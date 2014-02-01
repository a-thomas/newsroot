package com.codexperiments.newsroot.ui.fragment.authorization;

import com.codexperiments.newsroot.common.event.BaseEvent;
import com.codexperiments.newsroot.common.event.EventListener;

public class AuthorizedEvent extends BaseEvent<AuthorizedEvent.Listener>
{
    public AuthorizedEvent()
    {
        super();
    }

    @Override
    protected void notify(Listener pListener)
    {
        pListener.onAuthorized();
    }


    public interface Listener extends EventListener
    {
        void onAuthorized();
    }
}
