package com.codexperiments.newsroot.ui.timeline;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.Views;
import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.core.domain.entities.Tweet;
import com.codexperiments.newsroot.core.domain.entities.User;
import com.codexperiments.newsroot.data.sqlite.SqliteTweetRepository;

public class TweetItemView extends RelativeLayout {
    // State
    private Tweet tweet;
    // UI
    @InjectView(R.id.item_tweet_name) TextView nameView;
    @InjectView(R.id.item_tweet_screenname) TextView screenNameView;
    @InjectView(R.id.item_tweet_text) TextView textView;
    @InjectView(R.id.item_tweet_createdAt) TextView createdAtView;

    public static TweetItemView create(Activity activity, ViewGroup parent) {
        return (TweetItemView) activity.getLayoutInflater().inflate(R.layout.item_tweet, parent, false);
    }

    public static TweetItemView createOrRecycle(Activity activity, ViewGroup parent, View convertView) {
        TweetItemView tweetItemView;
        if (convertView != null) tweetItemView = (TweetItemView) convertView;
        else tweetItemView = (TweetItemView) activity.getLayoutInflater().inflate(R.layout.item_tweet, parent, false);
        return tweetItemView;
    }

    public TweetItemView(Context context, AttributeSet attrSet, int defStyle) {
        super(context, attrSet, defStyle);
    }

    public TweetItemView(Context context, AttributeSet attrSet) {
        super(context, attrSet);
    }

    public TweetItemView(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Views.inject(this);
    }

    public Tweet getTweet() {
        return tweet;
    }

    protected void setContent(Tweet tweet) {
        this.tweet = tweet;
        User user = tweet.getUser();

        nameView.setText(user.getName());
        screenNameView.setText(user.getScreenName());
        textView.setText(tweet.getText());
        createdAtView.setText(String.valueOf(tweet.getCreatedAt()));
    }

    public void setContent(SqliteTweetRepository.Mapper tweetMapper, Cursor cursor) {
        nameView.setText(tweetMapper.userMapper.getName(cursor));
        screenNameView.setText(tweetMapper.userMapper.getScreenName(cursor));
        textView.setText(tweetMapper.getText(cursor));
        createdAtView.setText(String.valueOf(tweetMapper.getCreatedAt(cursor)));
    }
//        setActivated(tweet.isSelected());
//
//    public void toggleSelection() {
//        boolean selected = !tweet.isSelected();
//        tweet.setSelected(selected);
//        setActivated(selected);
//    }
}
