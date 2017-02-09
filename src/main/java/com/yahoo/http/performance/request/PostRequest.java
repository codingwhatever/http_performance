// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.request;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

/**
 * A POST request.
 */
public class PostRequest extends Request {
    String postData;
    public PostRequest(String url, String postData) {
        super(RequestType.POST, url);
        this.postData = postData;
    }

    @Override
    public CloseableHttpResponse makeRequest(CloseableHttpClient httpClient) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(postData));
        return httpClient.execute(post);
    }
}
