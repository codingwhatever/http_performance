// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance.request;

import java.io.IOException;
import java.text.ParseException;

/**
 * Delay between requests.
 */
public class RequestDelay {
    private final long millis;
    private final int nanos;

    private RequestDelay(long millis, int nanos) {
        this.millis = millis;
        this.nanos = nanos;
    }

    public static RequestDelay parseInput(String delay) throws IOException {
        String[] delaySplit = delay.split("\\.");
        if (delaySplit.length > 2 || delaySplit.length < 1) {
            throw new IOException("Invalid request delay: " + delay);
        } else if (delaySplit.length == 1) {
            delaySplit = new String[] {delaySplit[0], "0"};
        }
        String millisString = delaySplit[0];
        String nanosString = delaySplit[1];
        long milliValue = 0;
        int nanosValue = 0;
        if (millisString.length() != 0) {
            milliValue = Long.valueOf(millisString);
        }
        if (nanosString.length() > 0 && nanosString.length() <= 6) {
            nanosValue = Integer.valueOf(nanosString);
        } else if (nanosString.length() > 6) {
            throw new IOException("Invalid request delay: " + delay);
        }
        return new RequestDelay(milliValue, nanosValue);
    }

    public long getMillis() {
        return millis;
    }

    public int getNanos() {
        return nanos;
    }
}
