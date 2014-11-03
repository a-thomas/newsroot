package com.codexperiments.newsroot.ui.timeline;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.InjectView;
import butterknife.Views;
import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.core.provider.TweetItemViewModel;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import com.codexperiments.newsroot.ui.BaseFragment;
import com.codexperiments.quickdao.sqlite.SQLiteCursorList;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

import static android.support.v7.widget.RecyclerView.Adapter;
import static android.support.v7.widget.RecyclerView.LayoutManager;
import static com.codexperiments.newsroot.NewsRootApplication.from;

public class TimecardFragment extends BaseFragment {
    // Dependencies
    @Inject SqliteTwitterDatabase twitterDatasource;
    @Inject TweetRepository tweetRepository;
//    @Inject TimelineProvider timelineProvider;
    // State
    protected SQLiteCursorList<TweetItemViewModel> cursorList = SQLiteCursorList.empty();
    protected CompositeSubscription subscriptions = new CompositeSubscription();
    // UI
    @InjectView(R.id.recycler_view) RecyclerView recyclerView;
    private LayoutManager layoutManager;
    private Adapter<TimecardViewHolder> adapter;


    public static class TimecardViewHolder extends RecyclerView.ViewHolder {
        public TweetItemView tweetItemView;

        public TimecardViewHolder(TweetItemView tweetItemView) {
            super(tweetItemView);
            this.tweetItemView = tweetItemView;
        }
    }

    public static final TimecardFragment forUser() {
        TimecardFragment fragment = new TimecardFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    //region Liefecycle
    @Override
    public View onCreateView(final LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        super.onCreateView(layoutInflater, container, bundle);
        View fragment = layoutInflater.inflate(R.layout.fragment_timecard, container, false);
        Views.inject(this, fragment);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        from(getActivity()).inject(this);

        adapter = new Adapter<TimecardViewHolder>() {
            @Override
            public TimecardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TweetItemView tweetItemView = TweetItemView.create(getActivity(), parent);
                return new TimecardViewHolder(tweetItemView);
            }

            @Override
            public void onBindViewHolder(TimecardViewHolder holder, int position) {
                TweetItemViewModel tweetItemViewModel = cursorList.get(position);
                holder.tweetItemView.setContent(tweetItemViewModel);
            }

            @Override
            public int getItemCount() {
                return cursorList.size();
            }
        };
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        loadTimeline();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursorList.close();
    }

    @Override
    public void onSaveInstanceState(Bundle pBundle) {
    }
    //endregion


    //region Actions
    protected void loadTimeline() {
//        timelineProvider.findTweets()
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Action1<SQLiteCursorList<TweetItemViewModel>>() {
//                            @Override
//                            public void call(SQLiteCursorList<TweetItemViewModel> sqliteCursorList) {
//                                cursorList = sqliteCursorList;
//                                adapter.notifyDataSetChanged();
//                            }
//                        });
    }
    //endregion


    //region Utilities
    protected void sub(Subscription subscription) {
        subscriptions.add(subscription);
    }
    //endregion
}