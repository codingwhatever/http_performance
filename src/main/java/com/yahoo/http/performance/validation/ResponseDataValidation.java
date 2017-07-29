// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yahoo.http.performance.request.Request;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Validates that response contains some json map.
 */
public class ResponseDataValidation extends Validation {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseDataValidation.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ResponseDataValidation() {
        super(ResponseDataValidation.class.getName());
    }

    @Override
    public boolean isValid(final Request request, final CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();
        String result = null;
        try {
            result = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Failed to read response", e);
            return false;
        }

        return result.equals(request.getExpectedResponseData());
    }
}
