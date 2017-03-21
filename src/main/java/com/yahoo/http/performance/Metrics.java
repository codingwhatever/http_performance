// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.List;

/**
 * Class for calculating test metrics.
 */
public class Metrics {
    private long threadCount;
    private long totalRequestCount;
    private long requestCountPerThread;
    private long totalTestTime;
    private long totalFailedRequests;
    private long requestsPerSecond;
    private String requestDelay;
    private DescriptiveStatistics statistics = new DescriptiveStatistics();

    public Metrics(List<ClientThread> threads) {
        ClientThread firstThread = threads.get(0);
        this.threadCount = threads.size();
        this.requestCountPerThread = firstThread.getRequestCount();
        this.requestDelay = firstThread.getRequestDelay().getMillis() + "." + firstThread.getRequestDelay().getNanos();
        this.totalRequestCount = threadCount * requestCountPerThread;

        this.totalTestTime = threads.stream().map(ClientThread::getRunTime).mapToLong(Long::longValue).sum() / threadCount;

        this.totalFailedRequests = threads.stream().map(ClientThread::getFailedRequest).mapToLong(Long::longValue).sum();

        for (int r = 0; r < requestCountPerThread; r++) {
            for (ClientThread thread : threads) {
                statistics.addValue(thread.getLatencies()[r]);
            }
        }

        long testTimeInSeconds = (totalTestTime / 1000);
        this.requestsPerSecond = totalRequestCount / (testTimeInSeconds == 0 ? 1 : testTimeInSeconds);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCurrent test metrics:");
        sb.append("\nThread Count: " + threadCount);
        sb.append("\nTotal Request Count: " + totalRequestCount);
        sb.append("\nRequest Count Per Thread: " + requestCountPerThread);
        sb.append("\nTotal Test Time (milli): " + totalTestTime);
        sb.append("\nRequest delay (milli): " + requestDelay);
        sb.append("\nTotal Failed Requests: " + totalFailedRequests);
        sb.append("\nRequests Per Second: " + requestsPerSecond + "\n");
        sb.append("\nLatency Metrics in nanoseconds:\n");
        sb.append("\nAverage Request Latency: " + statistics.getMean());
        sb.append("\nMin Request Latency: " + statistics.getMin());
        sb.append("\nMax Request Latency: " + statistics.getMax());
        sb.append("\nLatency Standard Deviation: " + statistics.getStandardDeviation());
        for (double percentile = 10; percentile < 100; percentile+=10) {
            sb.append("\nLatency percentile " + percentile + "%: " + statistics.getPercentile(percentile));
        }
        for (double percentile = 91; percentile < 100; percentile++) {
            sb.append("\nLatency percentile " + percentile + "%: " + statistics.getPercentile(percentile));
        }
        return sb.toString();
    }

    public long getThreadCount() {
        return threadCount;
    }

    public long getTotalRequestCount() {
        return totalRequestCount;
    }

    public long getRequestCountPerThread() {
        return requestCountPerThread;
    }

    public long getTotalTestTime() {
        return totalTestTime;
    }

    public long getTotalFailedRequests() {
        return totalFailedRequests;
    }

    public long getRequestsPerSecond() {
        return requestsPerSecond;
    }
}
