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
        String[] delaySplit = delay.split(".");
        String millisString = delaySplit[0];
        String nanosString = delaySplit[1];
        long milliValue = 0;
        int nanosValue = 0;
        if (millisString.length() != 0) {
            milliValue = Long.valueOf(millisString);
        }
        if (nanosString.length() > 0 && nanosString.length() <= 3) {
            nanosValue = Integer.valueOf(nanosString);
        } else if (nanosString.length() > 3) {
            throw new IOException("Invalid request delay: " + delay + ". example valid delays: .123, 1234.567, 123.");
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
