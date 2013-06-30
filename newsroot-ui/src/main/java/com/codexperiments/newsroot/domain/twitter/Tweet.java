package com.codexperiments.newsroot.domain.twitter;


public class Tweet implements Timeline.Item
{
    private long mId;
    // @DatabaseField(columnName = "TWT_TML_ID", canBeNull = false)
    // private long mTimelineId;
    private String mName;
    private String mScreenName;
    private String mText;
    private long mCreatedAt;

    public Tweet()
    {
        super();
        mId = -1;
    }

    public long getId()
    {
        return mId;
    }

    public void setId(long pId)
    {
        mId = pId;
    }

    @Override
    public long getTimelineId()
    {
        return mId;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String pName)
    {
        mName = pName;
    }

    public String getScreenName()
    {
        return mScreenName;
    }

    public void setScreenName(String pScreenName)
    {
        mScreenName = pScreenName;
    }

    public String getText()
    {
        return mText;
    }

    public void setText(String pText)
    {
        mText = pText;
    }

    public long getCreatedAt()
    {
        return mCreatedAt;
    }

    public void setCreatedAt(long pCreatedAt)
    {
        mCreatedAt = pCreatedAt;
    }
}
