package com.codexperiments.newsroot.presentation;

import rx.Observable;

import com.codexperiments.newsroot.common.rx.BooleanProperty;
import com.codexperiments.newsroot.common.rx.Command;
import com.codexperiments.newsroot.domain.twitter.TimeGap;

public class TimeGapPresentation implements NewsPresentation
{
    private TimeGap mTimeGap;
    private BooleanProperty mSelected;
    
    private Command<Void, Boolean> mToggleSelection;

    public TimeGapPresentation(TimeGap pTimeGap) {
        super();
        mTimeGap = pTimeGap;
        mSelected = BooleanProperty.create();
    }
    
    public TimeGap getTimeGap() {
        return mTimeGap;
    }

//    public Command<Void, ?> toggleSelection() {
//        if (mToggleSelection == null) {
//            mToggleSelection = Command.create(mSelected.toggle());
//            mToggleSelection.subscribe(mSelected);
//        }
//        return mToggleSelection;
//    }
//    
//    public Observable<Boolean> isSelected() {
//        return mSelected;
//    }
}