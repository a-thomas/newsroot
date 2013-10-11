package com.codexperiments.newsroot.presentation;

import rx.util.functions.Action1;

import com.codexperiments.newsroot.common.rx.Command;
import com.codexperiments.newsroot.common.rx.Property;
import com.codexperiments.newsroot.common.rx.RxUI;
import com.codexperiments.newsroot.domain.twitter.TimeGap;

public class TimeGapPresentation implements NewsPresentation
{
    public static enum State { LOADING, NONE }
    
    private NewsListPresentation mParent;
    private TimeGap mTimeGap;
    private Property<State> mState;
    
    private Command<Void, Void> mToggleSelection;

    public TimeGapPresentation(NewsListPresentation pParent, TimeGap pTimeGap) {
        super();
        mParent = pParent;
        mTimeGap = pTimeGap;
        mState = Property.create(State.NONE);
        
        mState.where(RxUI.eq(State.LOADING)).map(RxUI.toConstant(mTimeGap)).subscribe(mParent.findGapCommand());
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
    public Command<Void, ?> toggleSelection() {
        if (mToggleSelection == null) {
//            mToggleSelection = Command.create(null);
            mToggleSelection = Command.create();
            mToggleSelection.subscribe(new Action1<Void>() {
                                 public void call(Void pVoid) {
                                     mState.setIfNew(State.LOADING);
                                 }
                             });
//            new Func1<Void, Void>() {
//                public Void call(Void pVoid) {
//                    if (mState.get() != State.LOADING) {
//                        mState.set(State.LOADING);
//                    }
//                    return null;
//                }
//            }
//            mToggleSelection = Command.create();
//            mToggleSelection.subscribe(mState);
        }
        return mToggleSelection;
    }
    
    public Property<State> state() {
        return mState;
    }
}