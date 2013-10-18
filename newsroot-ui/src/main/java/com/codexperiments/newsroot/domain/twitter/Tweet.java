package com.codexperiments.newsroot.domain.twitter;


public class Tweet implements News {
    static int iiii = 0;

    private long mId;
    private int ii;
    // @DatabaseField(columnName = "TWT_TML_ID", canBeNull = false)
    // private long mTimelineId;
    private String mName;
    private String mScreenName;
    private String mText;
    private long mCreatedAt;

    private boolean mSelected;

    public Tweet() {
        super();
        ii = iiii++;
        mId = -1;
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        mId = pId;
    }

    @Override
    public long getTimelineId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public void setScreenName(String pScreenName) {
        mScreenName = pScreenName;
    }

    public String getText() {
        return mText;
    }

    public void setText(String pText) {
        mText = pText;
    }

    public long getCreatedAt() {
        return ii + mCreatedAt;
    }

    public void setCreatedAt(long pCreatedAt) {
        mCreatedAt = pCreatedAt;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean pSelected) {
        mSelected = pSelected;
    }

    @Override
    public String toString() {
        return "Tweet [mId=" + mId + ", mName=" + ii + "]";
    }
}
