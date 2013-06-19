package com.codexperiments.newsroot.ui.fragment;

import twitter4j.Status;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;

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

    public void setContent(Status pNews)
    {
        mUINewsName.setText(pNews.getUser().getName());
        mUINewsScreenName.setText(pNews.getUser().getScreenName());
        mUINewsText.setText(pNews.getText());
        mUINewsCreatedAt.setText(pNews.getCreatedAt().toString());
    }
}
