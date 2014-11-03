package com.codexperiments.newsroot.ui.timeline;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.InjectView;
import butterknife.Views;
import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.core.provider.TweetItemViewModel;

public class TweetItemView extends RelativeLayout {
    // State
    private TweetItemViewModel tweetItemViewModel;
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

    public TweetItemViewModel getViewModel() {
        return tweetItemViewModel;
    }

    protected void setContent(TweetItemViewModel tweetItemViewModel) {
        this.tweetItemViewModel = tweetItemViewModel;

        nameView.setText(tweetItemViewModel.userName);
        screenNameView.setText(tweetItemViewModel.userScreenName);
        textView.setText(tweetItemViewModel.tweetText);
        createdAtView.setText(String.valueOf(tweetItemViewModel.tweetCreatedAt));
    }

//        setActivated(tweet.isSelected());
//
//    public void toggleSelection() {
//        boolean selected = !tweet.isSelected();
//        tweet.setSelected(selected);
//        setActivated(selected);
//    }
}
