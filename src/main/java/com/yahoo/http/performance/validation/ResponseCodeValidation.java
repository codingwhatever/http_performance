// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.validation;

import com.yahoo.http.performance.request.Request;
import org.apache.http.client.methods.CloseableHttpResponse;

/**
 * A validation that ensures that the response code is 200.
 */
public class ResponseCodeValidation implements Validation {
    @Override
    public boolean isValid(final Request request, final CloseableHttpResponse response) {
        return response.getStatusLine().getStatusCode() == 200;
    }

    @Override
    public String toString() {
        return ResponseCodeValidation.class.getName();
    }
}
