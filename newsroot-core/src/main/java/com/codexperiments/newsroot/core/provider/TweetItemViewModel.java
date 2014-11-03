package com.codexperiments.newsroot.core.provider;

public class TweetItemViewModel {
    public long tweetId;
    public long tweetVersion;
    public String tweetText;
    public long tweetCreatedAt;

    public long userId;
    public long userVersion;
    public String userName;
    public String userScreenName;

    @Override
    public String toString() {
        return "TweetItemViewModel{" +
                "tweetId=" + tweetId +
                ", tweetVersion=" + tweetVersion +
                ", tweetText='" + tweetText + '\'' +
                ", tweetCreatedAt=" + tweetCreatedAt +
                ", userId=" + userId +
                ", userVersion=" + userVersion +
                ", userName='" + userName + '\'' +
                ", userScreenName='" + userScreenName + '\'' +
                '}';
    }
}
