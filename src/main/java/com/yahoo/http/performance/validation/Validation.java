// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.validation;

import com.yahoo.http.performance.request.Request;
import org.apache.http.client.methods.CloseableHttpResponse;

/**
 * Super class for all Validations.
 */
public abstract class Validation {
    private String name;

    public Validation(String name) {
        this.name = name;
    }

    public abstract boolean isValid(Request request, CloseableHttpResponse response);

    public String getName() {
        return name;
    }
}
