package com.codexperiments.newsroot.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.robolabor.task.id.TaskId;
import com.codexperiments.robolabor.task.util.TaskAdapter;

public class NewsFragment extends Fragment
{
    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;

    private List<Tweet> mTweets;
    private boolean mHasMore; // TODO

    private NewsAdapter mUIListAdapter;
    private ListView mUIList;
    private ProgressDialog mUIDialog;

    public static final NewsFragment home()
    {
        NewsFragment lFragment = new NewsFragment();
        Bundle lArguments = new Bundle();
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(LayoutInflater pLayoutInflater, ViewGroup pContainer, Bundle pBundle)
    {
        super.onCreateView(pLayoutInflater, pContainer, pBundle);
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterManager = BaseApplication.getServiceFrom(getActivity(), TwitterManager.class);

        mTweets = new ArrayList<Tweet>(100); // TODO Get from prefs.
        mHasMore = true;

        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);
        mUIListAdapter = new NewsAdapter(pLayoutInflater, mTweets, mHasMore, new NewsAdapter.Callback() {
            @Override
            public void onLoadMore()
            {
                loadMoreTweets();
            }
        });
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setAdapter(mUIListAdapter);
        mUIDialog = new ProgressDialog(getActivity());

        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());
        return lUIFragment;
    }

    public void onInitializeInstanceState(Bundle pBundle)
    {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle)
    {
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mTaskManager.manage(this);
        loadMoreTweets();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mUIDialog.dismiss();
        mTaskManager.unmanage(this);
    }

    private void loadMoreTweets()
    {
        mTaskManager.execute(new TaskAdapter<List<Tweet>>() {
            TwitterManager lTwitterManager = mTwitterManager;

            @Override
            public TaskId getId()
            {
                return super.getId();
            }

            @Override
            public void onStart(boolean pIsRestored)
            {
                mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);
            }

            @Override
            public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception
            {
                return lTwitterManager.findOldTweets();
            }

            @Override
            public void onFinish(TaskManager pTaskManager, List<Tweet> pResult)
            {
                mUIDialog.dismiss();
                mTweets.addAll(pResult);
                mUIListAdapter.notifyDataSetChanged(mTweets);
            }

            @Override
            public void onFail(TaskManager pTaskManager, Throwable pException)
            {
                mUIDialog.dismiss();
                Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                pException.printStackTrace();
            }
        });
    }
}
