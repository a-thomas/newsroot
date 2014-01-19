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

    @Override
    public String toString() {
        return "TweetDTO [mId=" + mId + ", mName=" + mName + ", mScreenName=" + mScreenName + ", mText=" + mText
                        + ", mCreatedAt=" + mCreatedAt + ", mSelected=" + mSelected + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (mCreatedAt ^ (mCreatedAt >>> 32));
        result = prime * result + (int) (mId ^ (mId >>> 32));
        result = prime * result + ((mName == null) ? 0 : mName.hashCode());
        result = prime * result + ((mScreenName == null) ? 0 : mScreenName.hashCode());
        result = prime * result + (mSelected ? 1231 : 1237);
        result = prime * result + ((mText == null) ? 0 : mText.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TweetDTO other = (TweetDTO) obj;
        if (mCreatedAt != other.mCreatedAt) return false;
        if (mId != other.mId) return false;
        if (mName == null) {
            if (other.mName != null) return false;
        } else if (!mName.equals(other.mName)) return false;
        if (mScreenName == null) {
            if (other.mScreenName != null) return false;
        } else if (!mScreenName.equals(other.mScreenName)) return false;
        if (mSelected != other.mSelected) return false;
        if (mText == null) {
            if (other.mText != null) return false;
        } else if (!mText.equals(other.mText)) return false;
        return true;
    }
}
