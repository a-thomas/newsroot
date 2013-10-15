package com.codexperiments.newsroot.ui.fragment;

import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.rx.Command;
import com.codexperiments.newsroot.common.rx.Property2;
import com.codexperiments.newsroot.common.rx.Property2.PropertyProxy;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.Tweet;
import com.codexperiments.newsroot.presentation.NewsPresentation;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.PageAdapterItem;

public class NewsItem extends RelativeLayout implements PageAdapterItem<NewsItem.Model> {
    public static class Model implements NewsPresentation {
        public Tweet mTweet;
        public Boolean mSelected;

        public Model(Tweet pTweet) {
            this(pTweet, Boolean.FALSE);
        }

        public Model(Tweet pTweet, Boolean pSelected) {
            super();
            mTweet = pTweet;
            mSelected = pSelected;
        }
    }

    public Model mModel;

    // private TweetPresentation mPresentation;
    private CompositeSubscription mSubcriptions;
    private Property2<Boolean> mSelectedProperty;
    private Command<Void, Boolean> mToggleSelection;

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
    public void setContent(NewsItem.Model pTweetPresentationModel) {
        // if (mPresentation != null) {
        // mSubcriptions.unsubscribe();
        // mSubcriptions = Subscriptions.create();
        // }

        if (mToggleSelection == null) {
            mModel = pTweetPresentationModel;
            mSelectedProperty = Property2.create(new PropertyProxy<Boolean>() {
                public Boolean get() {
                    return mModel.mSelected;
                }

                public void set(Boolean pValue) {
                    mModel.mSelected = pValue;
                }
            });
            mToggleSelection = Command.create(Property2.toggle(mSelectedProperty));
            mToggleSelection.subscribe(mSelectedProperty);

            // mPresentation = new TweetPresentation(pTweetPresentationModel);
            mSubcriptions.add(mSelectedProperty.subscribe(RxUI.toActivated(this)));
            mSubcriptions.add(RxUI.fromClick(this).subscribe(mToggleSelection));
        } else {
            mModel = pTweetPresentationModel;
            mSelectedProperty.set(mModel.mSelected);
            // mPresentation.bind(pTweetPresentationModel);
        }

        Tweet lTweet = pTweetPresentationModel.mTweet;
        mUINewsName.setText(lTweet.getName());
        mUINewsScreenName.setText(lTweet.getScreenName());
        mUINewsText.setText(lTweet.getText());
        mUINewsCreatedAt.setText(String.valueOf(lTweet.getCreatedAt()));
    }
}
