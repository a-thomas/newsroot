package com.codexperiments.newsroot.repository.twitter;

import rx.Observer;

import com.fasterxml.jackson.core.JsonParser;

public class TwitterQuery {
    private StringBuilder mQuery;
    private boolean mHasParameters;

    public static TwitterQuery queryHome(String pHost) {
        return new TwitterQuery(pHost, "1.1/statuses/home_timeline.json");
    }

    private TwitterQuery(String pHost, String pQuery) {
        super();
        mQuery = new StringBuilder(pHost).append(pQuery);
        mHasParameters = false;
    }

    public TwitterQuery withParamIf(boolean pCondition, String pParam, String pValue) {
        if (pCondition) {
            if (!mHasParameters) {
                mQuery.append("?");
                mHasParameters = true;
            } else {
                mQuery.append("&");
            }
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
        TResult parse(JsonParser pParser) throws Exception;
    }

    public interface Handler2<TResult> {
        void parse(JsonParser pParser, Observer<TResult> pObserver) throws Exception;
    }
}
