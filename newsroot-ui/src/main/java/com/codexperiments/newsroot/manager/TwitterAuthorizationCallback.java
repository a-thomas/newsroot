package com.codexperiments.newsroot.manager;

import android.os.Parcel;
import android.os.Parcelable;

public class TwitterAuthorizationCallback implements Parcelable
{
    private String mAuthorizationUrl;
    private String mCallbackUrl;

    public TwitterAuthorizationCallback(String pAuthorizationUrl, String pCallbackUrl)
    {
        super();
        mAuthorizationUrl = pAuthorizationUrl;
        mCallbackUrl = pCallbackUrl;
    }

    public String getAuthorizationUrl()
    {
        return mAuthorizationUrl;
    }

    public boolean isCallbackUrl(String pUrl)
    {
        return pUrl.startsWith(mCallbackUrl);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Parcelable.Creator<TwitterAuthorizationCallback> CREATOR = new Parcelable.Creator<TwitterAuthorizationCallback>() {
        @Override
        public TwitterAuthorizationCallback createFromParcel(Parcel pParcel)
        {
            return new TwitterAuthorizationCallback(pParcel);
        }

        @Override
        public TwitterAuthorizationCallback[] newArray(int size)
        {
            return new TwitterAuthorizationCallback[size];
        }
    };

    protected TwitterAuthorizationCallback(Parcel pParcel)
    {
        mAuthorizationUrl = pParcel.readString();
        mCallbackUrl = pParcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mAuthorizationUrl);
        dest.writeString(mCallbackUrl);
    }
}
