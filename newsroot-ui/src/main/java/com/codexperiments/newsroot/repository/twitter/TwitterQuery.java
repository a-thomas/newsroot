package com.codexperiments.newsroot.repository.twitter;

import com.codexperiments.newsroot.domain.twitter.TimeGap;

public class TwitterQuery {
    public static final String URL_HOME = "1.1/statuses/home_timeline.json";

    private StringBuilder mQuery;
    private char mSeparator;
    private int mPageSize;
    private TimeGap mTimeGap;

    public static TwitterQuery query(String pHost, String pUrl) {
        return new TwitterQuery(pHost, pUrl);
    }

    private TwitterQuery(String pHost, String pQuery) {
        super();
        mQuery = new StringBuilder(pHost).append(pQuery);
        mSeparator = '?';
    }

    // public int getPageSize() {
    // return mPageSize;
    // }
    //
    // public TimeGap getTimeGap() {
    // return mTimeGap;
    // }

    public TwitterQuery withPageSize(int pPageSize) {
        mPageSize = pPageSize;
        withParam("count", pPageSize);
        return this;
    }

    public TwitterQuery withTimeGap(TimeGap pTimeGap) {
        if (!pTimeGap.isFutureGap()) {
            withParam("max_id", pTimeGap.earliestBound() - 1);
        }
        if (!pTimeGap.isPastGap()) {
            withParam("since_id", pTimeGap.oldestBound());
        }
        mTimeGap = pTimeGap;
        return this;
    }

    public void withParam(String pParam, String pValue) {
        mQuery.append(mSeparator);
        if (mSeparator == '?') mSeparator = '&';
        // TODO Use URLEncodedUtils
        mQuery.append(pParam).append("=").append(pValue);
    }

    public void withParam(String pParam, int pValue) {
        mQuery.append(mSeparator);
        if (mSeparator == '?') mSeparator = '&';
        // TODO Use URLEncodedUtils
        mQuery.append(pParam).append("=").append(Integer.toString(pValue));
    }

    public void withParam(String pParam, long pValue) {
        mQuery.append(mSeparator);
        if (mSeparator == '?') mSeparator = '&';
        // TODO Use URLEncodedUtils
        mQuery.append(pParam).append("=").append(Long.toString(pValue));
    }

    @Override
    public String toString() {
        return mQuery.toString();
    }
}
