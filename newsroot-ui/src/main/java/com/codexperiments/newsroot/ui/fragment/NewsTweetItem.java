package com.codexperiments.newsroot.ui.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.domain.twitter.Tweet;

public class NewsTweetItem extends RelativeLayout {
    // private CompositeSubscription mSubcriptions;
    // private Property<Boolean> mSelectedProperty;

    // private Tweet mTweet;
    private TextView mUINewsName;
    private TextView mUINewsScreenName;
    private TextView mUINewsText;
    private TextView mUINewsCreatedAt;

    public NewsTweetItem(Context pContext, AttributeSet pAttrSet, int pDefStyle) {
        super(pContext, pAttrSet, pDefStyle);
        initialize(pContext);
    }

    public NewsTweetItem(Context pContext, AttributeSet pAttrSet) {
        super(pContext, pAttrSet);
        initialize(pContext);
    }

    public NewsTweetItem(Context pContext) {
        super(pContext);
        initialize(pContext);
    }

    protected void initialize(Context pContext) {
        LayoutInflater.from(pContext).inflate(R.layout.item_news, this, true);
        mUINewsName = (TextView) findViewById(R.id.item_news_name);
        mUINewsScreenName = (TextView) findViewById(R.id.item_news_screenname);
        mUINewsText = (TextView) findViewById(R.id.item_news_text);
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_createdAt);
    }

    // @Override
    // public void initialize(Tweet pTweet) {
    // mTweet = pTweet;
    // mSubcriptions = Subscriptions.create();
    // mSelectedProperty = isSelectedProperty();
    //
    // mSubcriptions.add(RxUI.fromClick(this).subscribe(Property.toggle(mSelectedProperty)));
    // mSubcriptions.add(mSelectedProperty.subscribe(RxUI.toActivated(this)));
    // }

    public void setContent(Tweet pTweet) {
        // mTweet = pTweet;

        mUINewsName.setText(pTweet.getName());
        mUINewsScreenName.setText(pTweet.getScreenName());
        mUINewsText.setText(pTweet.getText());
        mUINewsCreatedAt.setText(String.valueOf(pTweet.getCreatedAt()));

        // mSelectedProperty.reset();
    }

    // protected Property<Boolean> isSelectedProperty() {
    // return Property.create(new PropertyAccess<Boolean>() {
    // public Boolean get() {
    // return mTweet.isSelected();
    // }
    //
    // public void set(Boolean pValue) {
    // mTweet.setSelected(pValue);
    // }
    // });
    // }
}
