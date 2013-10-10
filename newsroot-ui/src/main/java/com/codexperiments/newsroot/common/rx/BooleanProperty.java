package com.codexperiments.newsroot.common.rx;

import rx.util.functions.Func1;

public class BooleanProperty extends Property<Boolean> {
    public static BooleanProperty create() {
        return new BooleanProperty(Boolean.FALSE);
    }

    public static BooleanProperty create(Boolean pValue) {
        return new BooleanProperty(pValue);
    }

    public BooleanProperty(Boolean pValue) {
        super(pValue);
    }
    
    public Func1<Void, Boolean> toggle() {
        return new Func1<Void, Boolean>() {
            public Boolean call(Void pVoid) {
                return Boolean.valueOf(!mValue.booleanValue());
            }
        };
    }
}