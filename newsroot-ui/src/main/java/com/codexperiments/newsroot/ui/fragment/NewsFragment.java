package com.codexperiments.newsroot.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Paging;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.Tweet;
import com.codexperiments.newsroot.manager.twitter.TwitterManager;
import com.codexperiments.robolabor.task.TaskManager;
import com.codexperiments.robolabor.task.id.TaskId;
import com.codexperiments.robolabor.task.util.TaskAdapter;

public class NewsFragment extends Fragment
{
    private static final int DEFAULT_PAGE_SIZE = 20;

    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterManager mTwitterManager;

    private List<Tweet> mTweets;
    private boolean mHasMore;

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

        mTweets = new ArrayList<Tweet>(DEFAULT_PAGE_SIZE);
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
            Paging lPaging = new Paging((mTweets.size() / DEFAULT_PAGE_SIZE) + 1, DEFAULT_PAGE_SIZE);

            @Override
            public TaskId getId()
            {
                return super.getId();
            }

            @Override
            public void onStart(boolean pIsRestored)
            {
                mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving data ...", true);
            }

            @Override
            public List<Tweet> onProcess(TaskManager pTaskManager) throws Exception
            {
                return lTwitterManager.getTweets(lPaging);
            }

            @Override
            public void onFinish(TaskManager pTaskManager, List<Tweet> pResult)
            {
                mUIDialog.dismiss();
                mTweets.addAll(pResult);
                ((BaseAdapter) mUIList.getAdapter()).notifyDataSetChanged();
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
