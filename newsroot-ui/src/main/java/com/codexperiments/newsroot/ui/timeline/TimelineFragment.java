package com.codexperiments.newsroot.ui.timeline;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import butterknife.InjectView;
import butterknife.Views;
import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.repository.TweetRepository;
import com.codexperiments.newsroot.data.sqlite.SqliteTwitterDatabase;
import com.codexperiments.newsroot.ui.BaseFragment;
import com.codexperiments.quickdao.sqlite.SQLiteCursorList;
import com.codexperiments.quickdao.sqlite.SQLiteRetriever;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

import static com.codexperiments.newsroot.NewsRootApplication.from;

public class TimelineFragment extends BaseFragment {
    // Dependencies
    @Inject SqliteTwitterDatabase twitterDatabase;
    @Inject TweetRepository tweetRepository;
    // State
    protected SQLiteCursorList<Tweet> cursorList = SQLiteCursorList.empty();
    protected CompositeSubscription subscriptions = new CompositeSubscription();
    // UI
    @InjectView(android.R.id.list) ListView listView;


    public static final TimelineFragment forUser() {
        TimelineFragment fragment = new TimelineFragment();
        Bundle arguments = new Bundle();
        fragment.setArguments(arguments);
        return fragment;
    }

    //region Liefecycle
    @Override
    public View onCreateView(final LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        super.onCreateView(layoutInflater, container, bundle);
        View fragment = layoutInflater.inflate(R.layout.fragment_timeline, container, false);
        Views.inject(this, fragment);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        from(getActivity()).inject(this);

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return cursorList.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return cursorList.get(position).getId();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TweetItemView tweetItemView = TweetItemView.createOrRecycle(getActivity(), parent, convertView);
                Tweet tweet = cursorList.get(position);
                tweetItemView.setContent(tweet);
                return tweetItemView;
            }
        });
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
//        final TweetRepository.FindAllTweet query = tweetRepository.findAll().withUser().pagedBy(20);
//        sub(query.retrieve(SQLiteRetriever.asObservableList(Tweet.class))
//                 .subscribeOn(Schedulers.io())
//                 .observeOn(AndroidSchedulers.mainThread())
//                 .subscribe(new Action1<SQLiteCursorList<Tweet>>() {
//                     @Override
//                     public void call(SQLiteCursorList<Tweet> sqliteCursorList) {
//                         cursorList = sqliteCursorList;
//                         ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
//                     }
//                 }));
    }
    //endregion


    //region Utilities
    protected void sub(Subscription subscription) {
        subscriptions.add(subscription);
    }
    //endregion
}
