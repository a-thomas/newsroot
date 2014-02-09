package com.codexperiments.newsroot.test.server;

import org.simpleframework.http.Request;

/**
 * Configuration class that indicates to the server which response to return according to the request.
 */
public interface MockServerHandler {
    /**
     * Return the path of the asset file whose content has to be returned by server.
     * 
     * @param pRequest Request received by server.
     * @return Asset file path.
     */
    String getResponse(Request pRequest);
}
