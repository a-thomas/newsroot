package com.codexperiments.newsroot.domain;

public class Tweet
{
    private String mId;
    private String mName;
    private String mScreenName;
    private String mText;
    private String mCreatedAt;

    public String getId()
    {
        return mId;
    }

    public void setId(String pId)
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
