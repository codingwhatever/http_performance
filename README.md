# http_performance

## What is http_performance?

The http_performance tool is a http client framework based on [Apache HttpComponents](https://hc.apache.org/) client that is used to test high throughput for low latency requests.

## How to use?

1. The most simple way is to follow the [Steps to Run](README.md#steps-to-run) below.
2. You can also write your own client using the ClientThread.

## Steps to Run

* Install the latest version of maven
* `git clone https://github.com/codingwhatever/http_performance.git`
* `cd http_performance`
* `mvn clean package`
* For testing POST: `java -jar target/httpperformance-1.0.jar -u http://<host>:<port>/<path> -m POST --dataPath /tmp/postDataDir -c 10000 -t 1`
* For testing GET:`java -jar target/httpperformance-1.0.jar -u http://<host>:<port>/<path> -m GET -c 10000 -t 1`
* For details on args: `java -jar target/httpperformance-1.0.jar --help`

## License

* See [LICENSE.md](LICENSE.md)