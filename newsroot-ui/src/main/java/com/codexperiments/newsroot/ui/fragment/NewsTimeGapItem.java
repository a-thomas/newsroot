package com.codexperiments.newsroot.ui.fragment;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codexperiments.newsroot.R;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.codexperiments.newsroot.presentation.TimeGapPresentation;
import com.codexperiments.newsroot.ui.fragment.PageAdapter.PageAdapterItem;

public class NewsTimeGapItem extends RelativeLayout implements PageAdapterItem<TimeGapPresentation> {
    private TextView mUINewsCreatedAt;
    private TimeGapPresentation mPresentation;

    public NewsTimeGapItem(Context pContext, AttributeSet pAttrSet, int pDefStyle) {
        super(pContext, pAttrSet, pDefStyle);
    }

    public NewsTimeGapItem(Context pContext, AttributeSet pAttrSet) {
        super(pContext, pAttrSet);
    }

    public NewsTimeGapItem(Context pContext) {
        super(pContext);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mUINewsCreatedAt = (TextView) findViewById(R.id.item_news_timegap);
    }

    @Override
    public void setContent(TimeGapPresentation pTimeGapPresentation) {
        mPresentation = pTimeGapPresentation;

        TimeGap lTimeGap = pTimeGapPresentation.getTimeGap();
        mUINewsCreatedAt.setText(lTimeGap.earliestBound() + "==\n" + lTimeGap.oldestBound());

        RxUI.fromClick(this).subscribe(mPresentation.loadCommand());
        // mPresentation.isSelected().subscribe(RxUI.toActivated(this));
        mPresentation.loading().subscribe(RxUI.toActivated(this));
        mPresentation.loading().map(RxUI.not()).subscribe(RxUI.toEnabled(this));
    }
}
