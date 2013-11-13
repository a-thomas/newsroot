package com.codexperiments.newsroot.test.server;

import org.simpleframework.http.Request;

public interface MockServerHandler {
    String getResponseAsset(Request pRequest);
}
