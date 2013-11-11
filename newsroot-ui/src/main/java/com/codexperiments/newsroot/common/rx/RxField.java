package com.codexperiments.newsroot.common.rx;

public final class RxField implements Comparable<RxField> {
    public static int ID_COUNTER = 1;

    private int mId;

    public static final RxField ref() {
        return new RxField(++ID_COUNTER);
    }

    public RxField(int pId) {
        super();
        mId = pId;
    }

    @Override
    public int compareTo(RxField pOtherField) {
        if (mId > pOtherField.mId) return +1;
        else if (mId < pOtherField.mId) return -1;
        else return 0;
    };
}
