package com.codexperiments.newsroot.ui.fragment;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.util.BufferClosing;
import rx.util.functions.Func0;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.BaseApplication;
import com.codexperiments.newsroot.common.event.EventBus;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.Timeline;
import com.codexperiments.newsroot.repository.twitter.TwitterRepository;
import com.codexperiments.robolabor.task.TaskManager;

public class NewsFragment extends Fragment {
    private EventBus mEventBus;
    private TaskManager mTaskManager;
    private TwitterRepository mTwitterRepository;

    private Timeline mTimeline;
    private List<News> mTweets;
    private boolean mHasMore; // TODO

    private NewsAdapter mUIListAdapter;
    private ListView mUIList;
    private ProgressDialog mUIDialog;

    public static final NewsFragment home() {
        NewsFragment lFragment = new NewsFragment();
        Bundle lArguments = new Bundle();
        lFragment.setArguments(lArguments);
        return lFragment;
    }

    @Override
    public View onCreateView(LayoutInflater pLayoutInflater, ViewGroup pContainer, Bundle pBundle) {
        super.onCreateView(pLayoutInflater, pContainer, pBundle);
        mEventBus = BaseApplication.getServiceFrom(getActivity(), EventBus.class);
        mTaskManager = BaseApplication.getServiceFrom(getActivity(), TaskManager.class);
        mTwitterRepository = BaseApplication.getServiceFrom(getActivity(), TwitterRepository.class);

        mTimeline = new Timeline();
        mTweets = new ArrayList<News>(); // TODO Get from prefs.
        mHasMore = true;

        View lUIFragment = pLayoutInflater.inflate(R.layout.fragment_news_list, pContainer, false);
        mUIListAdapter = new NewsAdapter(pLayoutInflater, mTweets, mHasMore, new NewsAdapter.Callback() {
            @Override
            public void onLoadMore() {
                loadMoreTweets();
            }
        });
        mUIList = (ListView) lUIFragment.findViewById(android.R.id.list);
        mUIList.setAdapter(mUIListAdapter);
        mUIDialog = new ProgressDialog(getActivity());

        onInitializeInstanceState((pBundle != null) ? pBundle : getArguments());
        return lUIFragment;
    }

    public void onInitializeInstanceState(Bundle pBundle) {
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }

    @Override
    public void onStart() {
        super.onStart();
        mEventBus.registerListener(this);
        mTaskManager.manage(this);
        // loadMoreTweets();
        refresh();
    }

    @Override
    public void onStop() {
        super.onStop();
        mUIDialog.dismiss();
        mTaskManager.unmanage(this);
        mEventBus.unregisterListener(this);
    }

    public void refresh() {
        final Pair<Observable<News>, Observable<BufferClosing>> lTweetPair = mTwitterRepository.findLatestTweets(mTimeline);
        final Func0<Observable<BufferClosing>> controller = new Func0<Observable<BufferClosing>>() {
            public Observable<BufferClosing> call() {
                return lTweetPair.second;
            }
        };

        lTweetPair.first.buffer(controller).subscribe(new Observer<List<News>>() {
            public void onNext(List<News> pNews) {
                mTweets.addAll(pNews);
                mUIListAdapter.notifyDataSetChanged();
            }

            public void onCompleted() {
                mUIDialog.dismiss();
            }

            public void onError(Throwable pThrowable) {
                mUIDialog.dismiss();
                Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
                pThrowable.printStackTrace();
            }
        });

        // mTaskManager.execute(new TaskAdapter<List<News>>() {
        // TwitterRepository lTwitterRepository = mTwitterRepository;
        // Timeline lTimeline = mTimeline;
        //
        // @Override
        // public TaskId getId() {
        // return super.getId();
        // }
        //
        // @Override
        // public void onStart(boolean pIsRestored) {
        // mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);
        // }
        //
        // @Override
        // public List<News> onProcess(TaskNotifier pNotifier) throws Exception {
        // lTwitterRepository.findLatestTweets(lTimeline)
        // .observeOn(AndroidScheduler.getInstance())
        // .subscribe(new Observer<News>() {
        // public void onNext(News pArgs) {
        // TODO
        // }
        //
        // public void onCompleted() {
        // mUIDialog.dismiss();
        // }
        //
        // public void onError(Throwable pException) {
        // mUIDialog.dismiss();
        // Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
        // pException.printStackTrace();
        // }
        // });
        // return null;
        // }
        //
        // @Override
        // public void onFinish(List<News> pResult) {
        // // //mUIDialog.dismiss();
        // // mTweets.addAll(pResult);
        // // mTimeline.appendNewItems(mTweets);
        // // mUIListAdapter.notifyDataSetChanged(mTweets);
        // }
        //
        // @Override
        // public void onFail(Throwable pException) {
        // // //mUIDialog.dismiss();
        // // //Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
        // // //pException.printStackTrace();
        // }
        // });
        // mTaskManager.execute(new Task<List<Timeline.Item>>() {
        // TwitterRepository lTwitterRepository = mTwitterRepository;
        // Timeline lTimeline = mTimeline;
        //
        // public List<Timeline.Item> onProcess(TaskManager pTaskManager) throws Exception {
        // return lTwitterRepository.findLatestTweets(lTimeline);
        // }
        // }).onStart(new TaskStart() {
        // public void onStart(boolean pIsRestored) {
        // mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);
        // }
        // }).onFinish(new TaskFinished<List<Timeline.Item>>() {
        // public void onFinish(TaskManager pTaskManager, List<Timeline.Item> pResult) {
        // mUIDialog.dismiss();
        // // mTweets.addAll(pResult);
        // // mTimeline.appendNewItems(mTweets);
        // // mUIListAdapter.notifyDataSetChanged(mTweets);
        // }
        // }).onFail(new TaskFailure() {
        // public void onFail(TaskManager pTaskManager, Throwable pException) {
        // mUIDialog.dismiss();
        // Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
        // pException.printStackTrace();
        // }
        // });
    }

    private void loadMoreTweets() {
        // mTaskManager.execute(new TaskAdapter<List<News>>() {
        // TwitterRepository lTwitterRepository = mTwitterRepository;
        // Timeline lTimeline = mTimeline;
        //
        // @Override
        // public TaskId getId() {
        // return super.getId();
        // }
        //
        // @Override
        // public void onStart(boolean pIsRestored) {
        // mUIDialog = ProgressDialog.show(getActivity(), "Please wait...", "Retrieving tweets ...", true);
        // }
        //
        // @Override
        // public List<News> onProcess(TaskNotifier pNotifier) throws Exception {
        // return lTwitterRepository.findOlderTweets(lTimeline);
        // }
        //
        // @Override
        // public void onFinish(List<News> pResult) {
        // mUIDialog.dismiss();
        // // mTweets.addAll(pResult);
        // // mTimeline.appendOldItems(mTweets);
        // // mUIListAdapter.notifyDataSetChanged(mTweets);
        // }
        //
        // @Override
        // public void onFail(Throwable pException) {
        // mUIDialog.dismiss();
        // Toast.makeText(getActivity(), "Oups!!! Something happened", Toast.LENGTH_LONG).show();
        // pException.printStackTrace();
        // }
        // });
    }
}
