package com.codexperiments.newsroot.test.server;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.Matchers.argThat;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;

public class MockServerMatchers {
    public static class RequestUrlMatcher extends BaseMatcher<Request> {
        private final String mValue;

        public RequestUrlMatcher(String pValue) {
            mValue = pValue;
        }

        @Override
        public boolean matches(Object pObject) {
            if (pObject instanceof Request) {
                Request lRequest = (Request) pObject;
                return lRequest.getTarget().contains(mValue);
            } else {
                return false;
            }
        }

        @Override
        public void describeTo(Description desc) {
            desc.appendText("Contains URL ");
            desc.appendText(mValue);
        }
    }

    public static class RequestHasParamMatcher extends BaseMatcher<Request> {
        private final String mParam;

        public RequestHasParamMatcher(String pParam) {
            mParam = pParam;
        }

        @Override
        public boolean matches(Object pObject) {
            boolean lMatches = false;
            if (pObject instanceof Request) {
                Query lQuery = ((Request) pObject).getQuery();
                return lQuery.containsKey(mParam);
            }
            return lMatches;
        }

        @Override
        public void describeTo(Description desc) {
            desc.appendText("Has param ");
            desc.appendText(mParam);
        }
    }

    public static class RequestParamMatcher extends BaseMatcher<Request> {
        private final String mParam;
        private final String mValue;

        public RequestParamMatcher(String pParam, String pValue) {
            mParam = pParam;
            mValue = pValue;
        }

        @Override
        public boolean matches(Object pObject) {
            boolean lMatches = false;
            if (pObject instanceof Request) {
                Query lQuery = ((Request) pObject).getQuery();
                return lQuery.containsKey(mParam) && lQuery.containsValue(mValue);
            }
            return lMatches;
        }

        @Override
        public void describeTo(Description desc) {
            desc.appendText("Has param ");
            desc.appendText(mParam);
            desc.appendText(" with value ");
            desc.appendText(mValue);
        }
    }

    public static RequestUrlMatcher hasUrl(String pValue) {
        return new RequestUrlMatcher(pValue);
    }

    public static RequestHasParamMatcher hasQueryParam(String pParam) {
        return new RequestHasParamMatcher(pParam);
    }

    public static RequestParamMatcher hasQueryParam(String pParam, String pValue) {
        return new RequestParamMatcher(pParam, pValue);
    }

    public static RequestParamMatcher hasQueryParam(String pParam, long pValue) {
        return new RequestParamMatcher(pParam, Long.toString(pValue));
    }

    public static OngoingStubbing<String> whenRequest(MockServerHandler pHandler) {
        return Mockito.when(pHandler.getResponseAsset(argThat(any(Request.class))));
    }
}
