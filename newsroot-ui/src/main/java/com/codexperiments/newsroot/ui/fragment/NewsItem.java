package com.codexperiments.newsroot.ui.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.domain.twitter.Tweet;

public class NewsItem extends RelativeLayout implements Checkable
{
    private boolean mChecked;
    private TextView mUINewsName;
    private TextView mUINewsScreenName;
    private TextView mUINewsText;
    private TextView mUINewsCreatedAt;

    public NewsItem(Context pContext, AttributeSet pAttrSet, int pDefStyle)
    {
        super(pContext, pAttrSet, pDefStyle);
    }

    public NewsItem(Context pContext, AttributeSet pAttrSet)
    {
        super(pContext, pAttrSet);
    }

    public NewsItem(Context pContext)
    {
        super(pContext);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mChecked = false;
        mUINewsName = (TextView) findViewById(R.id.item_news_name);
        mUINewsScreenName = (TextView) findViewById(R.id.item_news_screenname);
        mUINewsText = (TextView) findViewById(R.id.item_news_text);
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_createdAt);
    }

    public void setContent(Tweet pTweet)
    {
        mUINewsName.setText(pTweet.getName());
        mUINewsScreenName.setText(pTweet.getScreenName());
        mUINewsText.setText(pTweet.getText());
        mUINewsCreatedAt.setText(String.valueOf(pTweet.getCreatedAt()));
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean pChecked) {
        if (mChecked != pChecked) {
            mChecked = pChecked;
            mUINewsCreatedAt.setText(mUINewsCreatedAt.getText()+"="+mChecked);
        }
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
