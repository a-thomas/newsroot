package com.codexperiments.newsroot.repository.twitter;

import com.fasterxml.jackson.core.JsonParser;

public class TwitterQuery {
    private StringBuilder mQuery;
    private boolean mHasParameters;

    public TwitterQuery(String pHost, String pQuery) {
        super();
        mQuery = new StringBuilder(pHost).append(pQuery);
        mHasParameters = false;
    }

    public TwitterQuery withParam(String pParam, String pValue) {
        if (!mHasParameters) {
            mQuery.append("?");
            mHasParameters = true;
        } else {
            mQuery.append("&");
        }
        // TODO Use URLEncodedUtils
        mQuery.append(pParam).append("=").append(pValue);
        return this;
    }

    public TwitterQuery withParam(String pParam, int pValue) {
        return withParam(pParam, Integer.toString(pValue));
    }

    public TwitterQuery withParam(String pParam, long pValue) {
        return withParam(pParam, Long.toString(pValue));
    }

    @Override
    public String toString() {
        return mQuery.toString();
    }

    public interface Handler<TResult> {
        TResult parse(JsonParser pParser) throws Exception;
    }
}
