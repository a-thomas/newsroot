package com.codexperiments.newsroot.ui.fragment.newslist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;

public class NewsMoreItem extends RelativeLayout {
    private TextView mUINewsMoreLabel;
    private ProgressBar mUINewsMoreProgress;

    public NewsMoreItem(Context pContext, AttributeSet pAttrSet, int pDefStyle) {
        super(pContext, pAttrSet, pDefStyle);
        initialize(pContext);
    }

    public NewsMoreItem(Context pContext, AttributeSet pAttrSet) {
        super(pContext, pAttrSet);
        initialize(pContext);
    }

    public NewsMoreItem(Context pContext) {
        super(pContext);
        initialize(pContext);
    }

    protected void initialize(Context pContext) {
        LayoutInflater.from(pContext).inflate(R.layout.item_news_more, this, true);
        mUINewsMoreLabel = (TextView) findViewById(R.id.item_news_more_label);
        mUINewsMoreProgress = (ProgressBar) findViewById(R.id.item_news_more_progress);
    }

    public void enable() {
        mUINewsMoreLabel.setEnabled(true);
        mUINewsMoreProgress.setEnabled(true);
    }
}
