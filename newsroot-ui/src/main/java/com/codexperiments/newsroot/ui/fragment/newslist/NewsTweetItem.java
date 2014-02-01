package com.codexperiments.newsroot.ui.fragment.newslist;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.data.tweet.TweetDTO;

public class NewsTweetItem extends RelativeLayout {
    // private CompositeSubscription mSubcriptions;
    // private Property<Boolean> mSelectedProperty;

    private TweetDTO mTweet;
    private TextView mUINewsName;
    private TextView mUINewsScreenName;
    private TextView mUINewsText;
    private TextView mUINewsCreatedAt;

    public static NewsTweetItem create(Activity pActivity, ViewGroup pParent) {
        return (NewsTweetItem) pActivity.getLayoutInflater().inflate(R.layout.item_news, pParent, false);
    }

    public NewsTweetItem(Context pContext, AttributeSet pAttrSet, int pDefStyle) {
        super(pContext, pAttrSet, pDefStyle);
    }

    public NewsTweetItem(Context pContext, AttributeSet pAttrSet) {
        super(pContext, pAttrSet);
    }

    public NewsTweetItem(Context pContext) {
        super(pContext);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mUINewsName = (TextView) findViewById(R.id.item_news_name);
        mUINewsScreenName = (TextView) findViewById(R.id.item_news_screenname);
        mUINewsText = (TextView) findViewById(R.id.item_news_text);
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_createdAt);
    }

    public void setContent(TweetDTO pTweet) {
        mTweet = pTweet;
        mUINewsName.setText(pTweet.getName());
        mUINewsScreenName.setText(pTweet.getScreenName());
        mUINewsText.setText(pTweet.getText());
        mUINewsCreatedAt.setText(String.valueOf(pTweet.getCreatedAt()));

        setActivated(pTweet.isSelected());
    }

    public void toggleSelection() {
        boolean lSelected = !mTweet.isSelected();
        mTweet.setSelected(lSelected);
        setActivated(lSelected);
    }
}
