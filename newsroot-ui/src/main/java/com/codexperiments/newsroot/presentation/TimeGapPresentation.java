package com.codexperiments.newsroot.presentation;

import com.codexperiments.newsroot.domain.twitter.TimeGap;

public class TimeGapPresentation implements NewsPresentation
{
    private TimeGap mTimeGap;

    public TimeGapPresentation(TimeGap pTimeGap) {
        super();
        mTimeGap = pTimeGap;
    }

    public TimeGap getTimeGap() {
        return mTimeGap;
    }
}