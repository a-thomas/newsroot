package com.codexperiments.newsroot.domain.twitter;

import com.j256.ormlite.field.DatabaseField;

public class Tweet
{
    @DatabaseField(id = true, columnName = "TWT_ID", canBeNull = false)
    private long mId;
    // @DatabaseField(columnName = "TWT_TML_ID", canBeNull = false)
    // private long mTimelineId;
    @DatabaseField(columnName = "TWT_NAME")
    private String mName;
    @DatabaseField(columnName = "TWT_SCREEN_NAME")
    private String mScreenName;
    @DatabaseField(columnName = "TWT_TEXT")
    private String mText;
    @DatabaseField(columnName = "TWT_CREATED_AT")
    private String mCreatedAt;

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

    public String getCreatedAt()
    {
        return mCreatedAt;
    }

    public void setCreatedAt(String pCreatedAt)
    {
        mCreatedAt = pCreatedAt;
    }
}
