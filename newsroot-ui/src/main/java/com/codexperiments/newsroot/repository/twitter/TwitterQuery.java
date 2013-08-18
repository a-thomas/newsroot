package com.codexperiments.newsroot.repository.twitter;

import rx.Observer;

import com.codexperiments.newsroot.domain.twitter.TimeGap;
import com.fasterxml.jackson.core.JsonParser;

public class TwitterQuery {
    private StringBuilder mQuery;
    private char mSeparator;
    private int mPageSize;
    private TimeGap mTimeGap;

    public static TwitterQuery queryHome(String pHost) {
        return new TwitterQuery(pHost, "1.1/statuses/home_timeline.json");
    }

    private TwitterQuery(String pHost, String pQuery) {
        super();
        mQuery = new StringBuilder(pHost).append(pQuery);
        mSeparator = '?';
    }

    public int getPageSize() {
        return mPageSize;
    }

    public TimeGap getTimeGap() {
        return mTimeGap;
    }

    public TwitterQuery withPageSize(int pPageSize) {
        mPageSize = pPageSize;
        withParam("count", pPageSize);
        return this;
    }

    public TwitterQuery withTimeGap(TimeGap pTimeGap) {
        if (!pTimeGap.isFutureGap()) {
            withParam("max_id", pTimeGap.getEarliestBound() - 1);
        }
        if (!pTimeGap.isPastGap()) {
            withParam("since_id", pTimeGap.getOldestBound());
        }
        mTimeGap = pTimeGap;
        return this;
    }

    public TwitterQuery withParamIf(boolean pCondition, String pParam, String pValue) {
        if (pCondition) {
            mQuery.append(mSeparator);
            if (mSeparator == '?') mSeparator = '&';
            // TODO Use URLEncodedUtils
            mQuery.append(pParam).append("=").append(pValue);
        }
        return this;
    }

    public TwitterQuery withParam(String pParam, String pValue) {
        return withParamIf(true, pParam, pValue);
    }

    public TwitterQuery withParamIf(boolean pCondition, String pParam, int pValue) {
        return withParamIf(pCondition, pParam, Integer.toString(pValue));
    }

    public TwitterQuery withParam(String pParam, int pValue) {
        return withParamIf(true, pParam, Integer.toString(pValue));
    }

    public TwitterQuery withParamIf(boolean pCondition, String pParam, long pValue) {
        return withParamIf(pCondition, pParam, Long.toString(pValue));
    }

    public TwitterQuery withParam(String pParam, long pValue) {
        return withParamIf(true, pParam, Long.toString(pValue));
    }

    @Override
    public String toString() {
        return mQuery.toString();
    }

    public interface Handler<TResult> {
        TResult parse(TwitterQuery pQuery, JsonParser pParser) throws Exception;
    }

    public interface Handler2<TResult> {
        void parse(JsonParser pParser, Observer<TResult> pObserver) throws Exception;
    }
}
