// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.Getter;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Class for calculating test metrics.
 */
public class Metrics {
    @Getter
    private long threadCount;
    @Getter
    private long totalRequestCount;
    @Getter
    private long requestCountPerThread;
    @Getter
    private long totalTestTime;
    @Getter
    private long totalFailedRequests;
    @Getter
    private long requestsPerSecond;
    @Getter
    private long requestDelay;
    @Getter
    private double minRequestLatency;
    @Getter
    private double maxRequestLatency;
    @Getter
    private double avgRequestLatency;
    @Getter
    private double latencyStandardDeviation;
    @Getter
    private Map<Integer, Double> percentiles;

    public Metrics(List<ClientThread> threads) {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        ClientThread firstThread = threads.get(0);
        this.threadCount = threads.size();
        this.requestCountPerThread = firstThread.getRequestCount();
        this.requestDelay = firstThread.getRequestDelay();
        this.totalRequestCount = threadCount * requestCountPerThread;

        this.totalTestTime = threads.stream().map(ClientThread::getRunTime).mapToLong(Long::longValue).sum() / threadCount;

        this.totalFailedRequests = threads.stream().map(ClientThread::getFailedRequest).mapToLong(Long::longValue).sum();

        for (int r = 0; r < requestCountPerThread; r++) {
            for (ClientThread thread : threads) {
                statistics.addValue(thread.getLatencies()[r]);
            }
        }

        double testTimeInSeconds = (totalTestTime / 1000);
        this.requestsPerSecond = (long) (totalRequestCount / (testTimeInSeconds == 0.0 ? 1.0 : testTimeInSeconds));

        minRequestLatency = statistics.getMin();
        maxRequestLatency = statistics.getMax();
        avgRequestLatency = statistics.getMean();
        latencyStandardDeviation = statistics.getStandardDeviation();

        percentiles = Stream.of(IntStream.rangeClosed(1, 9).map(x -> 10 * x), IntStream.range(91, 100))
                .flatMapToInt(x -> x)
                .mapToObj(p -> new AbstractMap.SimpleEntry<>(p, statistics.getPercentile(Double.valueOf(p))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCurrent test metrics:");
        sb.append("\nThread Count: " + threadCount);
        sb.append("\nTotal Request Count: " + totalRequestCount);
        sb.append("\nRequest Count Per Thread: " + requestCountPerThread);
        sb.append("\nTotal Test Time (milli): " + totalTestTime);
        sb.append("\nRequest delay (nano): " + requestDelay);
        sb.append("\nTotal Failed Requests: " + totalFailedRequests);
        sb.append("\nRequests Per Second: " + requestsPerSecond + "\n");
        sb.append("\nLatency Metrics in nanoseconds:");
        sb.append("\nAverage Request Latency: " + avgRequestLatency);
        sb.append("\nMin Request Latency: " + minRequestLatency);
        sb.append("\nMax Request Latency: " + maxRequestLatency);
        sb.append("\nLatency Standard Deviation: " + latencyStandardDeviation);
        for (double percentile = 10; percentile < 100; percentile+=10) {
            sb.append("\nLatency percentile " + percentile + "%: " + percentiles.get(String.valueOf(percentile)));
        }
        for (double percentile = 91; percentile < 100; percentile++) {
            sb.append("\nLatency percentile " + percentile + "%: " + percentiles.get(String.valueOf(percentile)));
        }
        return sb.toString();
    }

    public String toJsonString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();;
        return gson.toJson(this);
    }
}
