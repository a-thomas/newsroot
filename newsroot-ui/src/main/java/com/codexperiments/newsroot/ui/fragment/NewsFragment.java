package com.codexperiments.newsroot.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.manager.TwitterManager;
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.robolabor.task.id.TaskId;
import com.codexperiments.robolabor.task.util.TaskAdapter;

public class NewsFragment extends Fragment
{
    private static final int DEFAULT_PAGE_SIZE = 20;

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;

    private List<Status> mTweets;
    private boolean mHasMore;

    private ListView mUIList;


    private static class TweetViewHolder
    {
        TextView mUIitle;
    }

    public static final NewsFragment home()
    {
        NewsFragment lFragment = new NewsFragment();
        Bundle lArguments = new Bundle();
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pBundle)
    {
        super.onCreateView(pInflater, pContainer, pBundle);
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterManager = BaseApplication.getServiceFrom(getActivity(), TwitterManager.class);

        mTweets = new ArrayList<Status>(DEFAULT_PAGE_SIZE);
        mHasMore = true;

        View lUIFragment = pInflater.inflate(R.layout.fragment_news_list, pContainer, false);
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);

        mUIList.setAdapter(new BaseAdapter() {
            @Override
            public View getView(int pPosition, View pConvertView, ViewGroup pParent)
            {
                return onDisplayTweet(pPosition, pConvertView, pParent);
            }

            @Override
            public long getItemId(int pPosition)
            {
                return mTweets.get(pPosition).getId();
            }

            @Override
            public Object getItem(int pPosition)
            {
                return mTweets.get(pPosition);
            }

            @Override
            public int getCount()
            {
                return mTweets.size();
            }
        });

        onRestoreInstanceState((pBundle != null) ? pBundle : getArguments());
        return lUIFragment;
    }

    public void onRestoreInstanceState(Bundle pBundle)
    {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle)
    {
    }

    @Override
    public void onStart()
    {
        mTaskManager.manage(this);
        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mTaskManager.unmanage(this);
    }

    private View onDisplayTweet(int pPosition, View pConvertView, ViewGroup pParent)
    {
        return null;
    }

    private void loadMoreTweets()
    {
        mTaskManager.execute(new TaskAdapter<List<Status>>() {
            @Override
            public TaskId getId()
            {
                return super.getId();
            }

            @Override
            public void onStart(boolean pIsRestored)
            {
                super.onStart(pIsRestored);
            }

            @Override
            public List<Status> onProcess(TaskManager pTaskManager) throws Exception
            {
                return super.onProcess(pTaskManager);
            }

            @Override
            public void onFinish(TaskManager pTaskManager, List<Status> pResult)
            {
                super.onFinish(pTaskManager, pResult);
            }

            @Override
            public void onFail(TaskManager pTaskManager, Throwable pException)
            {
                super.onFail(pTaskManager, pException);
            }
        });
    }
}
