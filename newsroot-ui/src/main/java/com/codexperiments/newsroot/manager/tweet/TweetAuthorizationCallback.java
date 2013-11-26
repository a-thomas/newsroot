package com.codexperiments.newsroot.manager.tweet;

import android.os.Parcel;
import android.os.Parcelable;

public class TweetAuthorizationCallback implements Parcelable {
    private String mAuthorizationUrl;
    private String mCallbackUrl;

    public TweetAuthorizationCallback(String pAuthorizationUrl, String pCallbackUrl) {
        super();
        mAuthorizationUrl = pAuthorizationUrl;
        mCallbackUrl = pCallbackUrl;
    }

    public String getAuthorizationUrl() {
        return mAuthorizationUrl;
    }

    public boolean isCallbackUrl(String pUrl) {
        return pUrl.startsWith(mCallbackUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<TweetAuthorizationCallback> CREATOR = new Parcelable.Creator<TweetAuthorizationCallback>() {
        @Override
        public TweetAuthorizationCallback createFromParcel(Parcel pParcel) {
            return new TweetAuthorizationCallback(pParcel);
        }

        @Override
        public TweetAuthorizationCallback[] newArray(int size) {
            return new TweetAuthorizationCallback[size];
        }
    };

    protected TweetAuthorizationCallback(Parcel pParcel) {
        mAuthorizationUrl = pParcel.readString();
        mCallbackUrl = pParcel.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthorizationUrl);
        dest.writeString(mCallbackUrl);
    }
}
