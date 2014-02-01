package com.codexperiments.newsroot.ui.fragment.newslist;

import com.codexperiments.newsroot.common.event.BaseEvent;
import com.codexperiments.newsroot.common.event.EventListener;

public class NewsLoadedEvent extends BaseEvent<NewsLoadedEvent.Listener>
{
    public NewsLoadedEvent()
    {
        super();
    }

    @Override
    protected void notify(Listener pListener)
    {
        pListener.onNewsLoaded();
    }


    public interface Listener extends EventListener
    {
        void onNewsLoaded();
    }
}
