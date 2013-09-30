package com.codexperiments.newsroot.ui.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.domain.twitter.News;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.domain.twitter.Tweet;

public class NewsItem extends RelativeLayout
{
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

        mUINewsName = (TextView) findViewById(R.id.item_news_name);
        mUINewsScreenName = (TextView) findViewById(R.id.item_news_screenname);
        mUINewsText = (TextView) findViewById(R.id.item_news_text);
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_createdAt);
    }

    public void setContent(News pNews)
    {
        if (pNews instanceof Tweet) {
            Tweet lTweet = (Tweet) pNews;
            mUINewsName.setText(lTweet.getName());
            mUINewsScreenName.setText(lTweet.getScreenName());
            mUINewsText.setText(lTweet.getText());
            mUINewsCreatedAt.setText(String.valueOf(lTweet.getCreatedAt()));
        } else if (pNews instanceof TimeGap) {
            TimeGap lTimeGap = (TimeGap) pNews;
            mUINewsName.setText(lTimeGap.earliestBound() + "==" + lTimeGap.oldestBound());
        }
    }
}
