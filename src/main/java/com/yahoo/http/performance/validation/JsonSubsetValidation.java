// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.validation;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Validates that response contains some json map.
 */
public class JsonSubsetValidation extends Validation {
    private static final Logger LOG = LoggerFactory.getLogger(JsonSubsetValidation.class);
    private final Map<String, Object> jsonDataGolden;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public JsonSubsetValidation(String jsonString) throws IOException {
        super(JsonSubsetValidation.class.getName());
        jsonDataGolden = MAPPER.readValue(jsonString, Map.class);
    }

    @Override
    public boolean isValid(final CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();
        Map<String, Object> responseJson;
        try {
            responseJson = MAPPER.readValue(entity.getContent(), Map.class);
        } catch (IOException e) {
            LOG.error("Invalid json in response.", e);
            return false;
        }
        for (String key : jsonDataGolden.keySet()) {
            if (!jsonDataGolden.get(key).equals(responseJson.get(key))) {
                LOG.error("Expected " + jsonDataGolden + " to be a subset of " + responseJson + " but this is not the case.");
                return false;
            }
        }
        return true;
    }
}
