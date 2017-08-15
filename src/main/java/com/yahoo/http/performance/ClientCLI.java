// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache 2.0 license. Please see LICENSE file in the project root for terms.

package com.yahoo.http.performance;

import com.yahoo.http.performance.request.GetRequest;
import com.yahoo.http.performance.request.PostRequest;
import com.yahoo.http.performance.request.Request;
import com.yahoo.http.performance.request.RequestType;
import com.yahoo.http.performance.validation.ResponseDataValidation;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Map<String, String> argMap = parseArgs(args);
        List<Request> requests = getRequests(argMap);
        List<Validation> validations = new ArrayList<>();
        if (Boolean.valueOf(argMap.get("responseCodeValidation"))) {
            validations.add(new ResponseCodeValidation());
        }

        if (Boolean.valueOf(argMap.get("postRequestValidation")) || argMap.get("getRequestValidation") != null) {
            validations.add(new ResponseDataValidation());
        }

        long requestDelay = Long.valueOf(argMap.get("requestDelay"));
        int count = Integer.valueOf(argMap.get("count"));
        boolean sslEnabled = Boolean.valueOf(argMap.get("sslEnabled"));

        List<ClientThread> clientThreads = new ArrayList();

        for (int i = 0; i < Integer.valueOf(argMap.get("threads")); i++) {
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

    private static List<Request> getRequests(Map<String, String> argMap) throws IOException {
        List<Request> requests = new ArrayList<>();

        RequestType type = RequestType.valueOf(argMap.get("method"));
        switch (type) {
            case GET:
                Request request = new GetRequest(argMap.get("url"));
                requests.add(request);
                break;
            case POST:
                requests = getPostRequests(
                            argMap.get("url"),
                            argMap.get("dataPath"),
                            requests,
                            Boolean.valueOf(argMap.get("postRequestValidation"))
                        );
                break;
            default:
                throw new IllegalStateException("Invalid method");
        }

        String expectedFilePath = argMap.get("getRequestValidation");
        if (expectedFilePath != null && type == RequestType.GET) {
            String expectedResponseData = new String(Files.readAllBytes(new File(expectedFilePath).toPath()));
            requests.forEach(request -> { request.setExpectedResponseData(expectedResponseData); });
        } else if (argMap.get("getRequestValidation") != null) {
            throw new RuntimeException("Invalid use of getRequestValidation argument");
        }

        return requests;
    }

    private static List<Request> getPostRequests(String url, String dataPath, List<Request> requests, boolean validationEnabled) throws IOException {
        File path = new File(dataPath);

        File[] files = path.listFiles(new FilenameFilter(){
            public boolean accept( File dir, String name ) {
                return ! name.matches( ".*\\.expected$" );
            }
        });
        for (File file : files) {
            String data = new String(Files.readAllBytes(file.toPath()));
            Request request = new PostRequest(url, data);

            if (validationEnabled) {
                Path expectedResultPath = new File(file.getPath().concat(".expected")).toPath();
                request.setExpectedResponseData(new String(Files.readAllBytes(expectedResultPath)));
            }

            requests.add(request);
        }

        return requests;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> argMap = new HashMap();

        Option numThreads = new Option("t", "threads", true, "Number of threads to run.");
        Option numRequests = new Option("c", "count", true, "Number of requests to send per thread.");
        Option url = new Option("u", "url", true, "Url to send request to.");
        Option requestType = new Option("m", "method", true, "Http method to use. eg. POST or GET");
        Option postDataPath = new Option("d", "dataPath", true, "Directory containing file data to be posted. Each file will be " +
                "posted independently.");
        Option responseCodeValidation = new Option("r", "responseCodeValidation", false, "Check that all requests give 200 response.");
        Option getRequestValidation = new Option("g", "getRequestValidation", true, "Only for GET requests: Validate the servers response " +
                "against some expected string read from the file specified in this arg.");
        Option postRequestValidation = new Option("p", "postRequestValidation", false, "Only for POST requests: Looks for a file with " +
                ".expected extension corresponding to each input file from postDataPath arg path.");
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
        options.addOption(getRequestValidation);
        options.addOption(requestDelay);
        options.addOption(sslEnabled);
        options.addOption(postRequestValidation);


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

        argMap.put("threads", cmd.getOptionValue("threads"));
        argMap.put("count", cmd.getOptionValue("count"));
        argMap.put("url", cmd.getOptionValue("url"));
        argMap.put("method", cmd.getOptionValue("method"));
        argMap.put("dataPath", cmd.getOptionValue("dataPath"));
        argMap.put("responseCodeValidation", String.valueOf(cmd.hasOption("responseCodeValidation")));
        argMap.put("getRequestValidation", cmd.getOptionValue("getRequestValidation"));
        argMap.put("requestDelay", cmd.getOptionValue("requestDelay"));
        argMap.put("sslEnabled", String.valueOf(cmd.hasOption("sslEnabled")));
        argMap.put("postRequestValidation", String.valueOf(cmd.hasOption("postRequestValidation")));

        return argMap;
    }
}
