package com.codexperiments.newsroot.ui.fragment;

import java.util.List;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.presentation.TweetPresentation;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.PageAdapterItem;

public class NewsItem extends RelativeLayout implements PageAdapterItem<TweetPresentation> {
    private TweetPresentation mPresentation;
    private CompositeSubscription mSubcriptions;

    private boolean mChecked;
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
        mChecked = false;
        mUINewsName = (TextView) findViewById(R.id.item_news_name);
        mUINewsScreenName = (TextView) findViewById(R.id.item_news_screenname);
        mUINewsText = (TextView) findViewById(R.id.item_news_text);
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_createdAt);
        
        mSubcriptions = Subscriptions.create();
    }

    @Override
    public void setContent(TweetPresentation pTweetPresentation) {
        if (mPresentation != null) {
            mSubcriptions.unsubscribe();
            mSubcriptions = Subscriptions.create();
        }
        
        mPresentation = pTweetPresentation;

        Tweet lTweet = pTweetPresentation.getTweet();
        mUINewsName.setText(lTweet.getName());
        mUINewsScreenName.setText(lTweet.getScreenName());
        mUINewsText.setText(lTweet.getText());
        mUINewsCreatedAt.setText(String.valueOf(lTweet.getCreatedAt()));
        
        mSubcriptions.add(mPresentation.isSelected().subscribe(RxUI.toActivated(this)));
        mSubcriptions.add(RxUI.fromClick(this).subscribe(mPresentation.toggleSelection()));
    }
}
