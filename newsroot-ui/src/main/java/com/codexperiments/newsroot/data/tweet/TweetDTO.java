package com.codexperiments.newsroot.data.tweet;

import com.codexperiments.newsroot.domain.tweet.News;

public class TweetDTO implements News {
    private long mId;
    private String mName;
    private String mScreenName;
    private String mText;
    private long mCreatedAt;
    private boolean mSelected;

    public TweetDTO() {
        super();
    }

    public long getId() {
        return mId;
    }

    public void setId(long pId) {
        mId = pId;
    }

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
        return mCreatedAt;
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
}
