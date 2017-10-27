// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.request;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * Super class for all request types.
 */
public abstract class Request {
    protected RequestType requestType;
    protected String url;

    private String expectedResponseData;

    public Request(RequestType type, String url) {
        this.requestType = type;
        this.url = url;
    }

    public abstract CloseableHttpResponse makeRequest(CloseableHttpClient httpClient) throws IOException;

    public String getExpectedResponseData() {
        return expectedResponseData;
    }

    public void setExpectedResponseData(String expectedResponseData) {
        this.expectedResponseData = expectedResponseData;
    }
}
