package com.codexperiments.newsroot.ui.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.domain.twitter.TimeGap;

public class NewsTimeGapItem extends RelativeLayout
{
    private TextView mUINewsCreatedAt;

    public NewsTimeGapItem(Context pContext, AttributeSet pAttrSet, int pDefStyle)
    {
        super(pContext, pAttrSet, pDefStyle);
    }

    public NewsTimeGapItem(Context pContext, AttributeSet pAttrSet)
    {
        super(pContext, pAttrSet);
    }

    public NewsTimeGapItem(Context pContext)
    {
        super(pContext);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_timegap);
    }

    public void setContent(TimeGap pTimeGap)
    {
        TimeGap lTimeGap = (TimeGap) pTimeGap;
        mUINewsCreatedAt.setText(lTimeGap.earliestBound() + "==\n" + lTimeGap.oldestBound());
    }
}
