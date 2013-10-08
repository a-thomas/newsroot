package com.codexperiments.newsroot.ui.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;

public class NewsMoreItem extends RelativeLayout
{
    private TextView mUINewsMoreLabel;
    private ProgressBar mUINewsMoreProgress;

    public NewsMoreItem(Context pContext, AttributeSet pAttrSet, int pDefStyle)
    {
        super(pContext, pAttrSet, pDefStyle);
    }

    public NewsMoreItem(Context pContext, AttributeSet pAttrSet)
    {
        super(pContext, pAttrSet);
    }

    public NewsMoreItem(Context pContext)
    {
        super(pContext);
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        mUINewsMoreLabel = (TextView) findViewById(R.id.item_news_more_label);
        mUINewsMoreProgress = (ProgressBar) findViewById(R.id.item_news_more_progress);
    }

    public void setContent()
    {
        mUINewsMoreLabel.setEnabled(true);
        mUINewsMoreProgress.setEnabled(true);
    }
}
