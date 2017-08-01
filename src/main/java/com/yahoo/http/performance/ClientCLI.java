// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance;

import com.yahoo.http.performance.request.*;
import com.yahoo.http.performance.validation.JsonSubsetValidation;
import com.yahoo.http.performance.validation.ResponseCodeValidation;
import com.yahoo.http.performance.validation.Validation;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The ClientCLI is the command line interface for running this perf testing framework.
 * Example usage:
 * java -jar httpperformance-1.0.jar -u [url] -m GET -c 500000 -t 64
 * java -jar httpperformance-1.0.jar -u [url] -m POST -d [postDataPath] -c 500000 -t 64
 *
 * See help options for more details.
 */
public class ClientCLI {
    public static void main(String[] args) throws Exception {
        Map<String, Object> argMap = parseArgs(args);
        List<Request> requests = getRequests(argMap);
        List<Validation> validations = new ArrayList<>();
        if ((boolean) argMap.get("responseCodeValidation")) {
            validations.add(new ResponseCodeValidation());
        }

        if (argMap.get("jsonSubsetValidation") != null) {
            String jsonFile = (String) argMap.get("jsonSubsetValidation");
            String jsonString = new String(Files.readAllBytes(new File(jsonFile).toPath()));
            validations.add(new JsonSubsetValidation(jsonString));
        }

        long requestDelay = (long) argMap.get("requestDelay");
        int count = (int) argMap.get("count");
        boolean sslEnabled = (boolean) argMap.get("sslEnabled");

        List<ClientThread> clientThreads = new ArrayList();

        for (int i = 0; i < (int) argMap.get("threads"); i++) {
            clientThreads.add(
                    new ClientThread(
                            count,
                            requests,
                            validations,
                            requestDelay,
                            sslEnabled
                    )
            );
        }

        List<Thread> threads = clientThreads.stream().map(t -> new Thread(t)).collect(Collectors.toList());

        threads.forEach(Thread::start);

        threads.forEach(t -> {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
        });

        Metrics metrics = new Metrics(clientThreads);

        System.out.print(metrics.toString());
    }

    private static List<Request> getRequests(Map<String, Object> argMap) throws IOException {
        ArrayList<Request> requests = new ArrayList<>();

        switch (RequestType.valueOf((String) argMap.get("method"))) {
            case GET:
                Request request = new GetRequest((String) argMap.get("url"));
                requests.add(request);
                return requests;
            case POST:
                return getPostRequests((String) argMap.get("url"), (String) argMap.get("dataPath"), requests);
            default:
                throw new IllegalStateException("Invalid method");
        }
    }

    private static List<Request> getPostRequests(String url, String dataPath, List<Request> requests) throws IOException {
        File path = new File(dataPath);

        File[] files = path.listFiles();
        for (File file : files) {
            String data = new String(Files.readAllBytes(file.toPath()));
            requests.add(new PostRequest(url, data));
        }

        return requests;
    }

    private static Map<String, Object> parseArgs(String[] args) {
        Map<String, Object> argMap = new HashMap();

        Option numThreads = new Option("t", "threads", true, "Number of threads to run.");
        Option numRequests = new Option("c", "count", true, "Number of requests to send per thread.");
        Option url = new Option("u", "url", true, "Url to send request to.");
        Option requestType = new Option("m", "method", true, "Http method to use. eg. POST or GET");
        Option postDataPath = new Option("d", "dataPath", true, "Directory containing file data to be posted. Each file will be " +
                "posted independently.");
        Option responseCodeValidation = new Option("r", "responseCodeValidation", false, "Check that all requests give 200 response.");
        Option jsonSubsetValidation = new Option("j", "jsonSubsetValidation", true, "Check that the given json map is a subset" +
                "of the response json. This assumes that the arg file and the response are single level json maps.");
        Option requestDelay = new Option("rd", "requestDelay", true, "Delay between each request in nanoseconds. The code will busy wait " +
                "instead of sleep inorder to allow smaller delays than 1ms.");
        Option sslEnabled = new Option("s", "sslEnabled", false, "Enables ssl support with a truststrategy that returns true instead " +
                "of verifying the certificate.");

        numThreads.setRequired(true);
        numRequests.setRequired(true);
        url.setRequired(true);
        requestType.setRequired(true);

        Options options = new Options();
        options.addOption(numThreads);
        options.addOption(numRequests);
        options.addOption(url);
        options.addOption(requestType);
        options.addOption(postDataPath);
        options.addOption(responseCodeValidation);
        options.addOption(jsonSubsetValidation);
        options.addOption(requestDelay);
        options.addOption(sslEnabled);


        CommandLineParser parser = new BasicParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            helpFormatter.printHelp("http-performance-client", options);

            System.exit(1);
        }

        argMap.put("threads", Integer.valueOf(cmd.getOptionValue("threads")));
        argMap.put("count", Integer.valueOf(cmd.getOptionValue("count")));
        argMap.put("url", cmd.getOptionValue("url"));
        argMap.put("method", cmd.getOptionValue("method"));
        argMap.put("dataPath", cmd.getOptionValue("dataPath"));
        argMap.put("responseCodeValidation", cmd.hasOption("responseCodeValidation"));
        argMap.put("jsonSubsetValidation", cmd.getOptionValue("jsonSubsetValidation"));
        argMap.put("requestDelay", Long.valueOf(cmd.getOptionValue("requestDelay")));
        argMap.put("sslEnabled", cmd.hasOption("sslEnabled"));

        return argMap;
    }
}
