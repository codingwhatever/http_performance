// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.request;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * A GET request.
 */
public class GetRequest extends Request {
    public GetRequest(String url) {
        super(RequestType.GET, url);
    }

    public CloseableHttpResponse makeRequest(CloseableHttpClient httpClient) throws IOException {
        HttpGet get = new HttpGet(url);
        return httpClient.execute(get);
    }
}
