package com.codexperiments.newsroot.ui.fragment;

import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.rx.Property;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.PageAdapterItem;

public class NewsItem extends RelativeLayout implements PageAdapterItem<Tweet> {
    private CompositeSubscription mSubcriptions;
    private Property<Boolean> mSelectedProperty;

    private TextView mUINewsName;
    private TextView mUINewsScreenName;
    private TextView mUINewsText;
    private TextView mUINewsCreatedAt;

    public NewsItem(Context pContext, AttributeSet pAttrSet, int pDefStyle) {
        super(pContext, pAttrSet, pDefStyle);
    }

    public NewsItem(Context pContext, AttributeSet pAttrSet) {
        super(pContext, pAttrSet);
    }

    public NewsItem(Context pContext) {
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

    protected void initialize(Tweet pTweet) {
        mSubcriptions = Subscriptions.create();
        mSelectedProperty = Tweet.selectedProperty(pTweet);

        mSubcriptions.add(RxUI.fromClick(this).subscribe(Property.toggle(mSelectedProperty)));
        mSubcriptions.add(mSelectedProperty.subscribe(RxUI.toActivated(this)));
    }

    @Override
    public void setContent(Tweet pTweet) {
        if (mSubcriptions == null) initialize(pTweet);

        mSelectedProperty.set(pTweet.isSelected());

        mUINewsName.setText(pTweet.getName());
        mUINewsScreenName.setText(pTweet.getScreenName());
        mUINewsText.setText(pTweet.getText());
        mUINewsCreatedAt.setText(String.valueOf(pTweet.getCreatedAt()));
    }
}
