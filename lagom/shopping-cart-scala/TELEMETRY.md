# Lightbend Telemetry

[Telemetry](https://developer.lightbend.com/docs/telemetry/current/home.html), part of Lightbend’s Intelligent Monitoring feature set, is a suite of insight tools that provides a view into the workings of our distributed platforms. This view allows developers and operations to respond quickly to problems, track down unexpected behavior and even tune your system. As a result, you can deploy your applications to production with confidence.

## Requirements

These are the requirements to run this project with Lightbend Telemetry.

- [sbt](https://www.scala-sbt.org/1.x/docs/Setup.html)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Lightbend account and Bintray credentials](https://developer.lightbend.com/docs/telemetry/current/getting-started/start.html#lightbend-account-and-bintray-credentials)

## About this sample

This project differs from the original Lagom sample by adding Lightbend Telemetry. To run it, besides the requirements listed below, you will also need [a Lightbend account and Bintray credentials](https://developer.lightbend.com/docs/telemetry/current/home.html). You can either edit `credentials.sbt` file to what is recommended in Lightbend Telemetry documentation, or set environment variables using:

```shell script
export LIGHTBEND_COMMERCIAL_MVN="<the-url-you-will-get-for-your-lightbend-account>"
export LIGHTBEND_COMMERCIAL_IVY="<the-url-you-will-get-for-your-lightbend-account>"
```

Then, the current `credentials.sbt` will read these environment variables.

## Running locally

Lagom `runAll` sbt command does not provide a way to attach agents. Because of that, you won't be able to see the Lightbend Telemetry agent in action when using such commands.

When agent is properly configured, you should see some output like:

```
[INFO] [11/20/2020 16:01:19.588] [main-1] [Cinnamon] Agent version 2.15.1
[INFO] [11/20/2020 16:01:19.869] [main-1] [Cinnamon] Agent found Play version: 2.8.2
[INFO] [11/20/2020 16:01:19.870] [main-1] [Cinnamon] Agent found Scala version: 2.13.2
[INFO] [11/20/2020 16:01:19.924] [main-1] [Cinnamon] Agent found Play-AHC-WS version: 2.8.2
[INFO] [11/20/2020 16:01:19.934] [main-1] [Cinnamon] Agent found Scala Futures version: 2.13.2
[INFO] [11/20/2020 16:01:20.120] [main-1] [Cinnamon] Agent found Java Futures version: 11.0.8
[INFO] [11/20/2020 16:01:20.794] [main-1] [Cinnamon] Agent found Lagom Projection version: 1.6.4
[INFO] [11/20/2020 16:01:21.059] [main-1] [Cinnamon] Agent found Akka Actor version: 2.6.8
[INFO] [11/20/2020 16:01:21.071] [main-1] [Cinnamon] Agent found Akka version: 2.6.8
[INFO] [11/20/2020 16:01:21.077] [main-1] [Cinnamon] Agent found Akka Cluster version: 2.6.8
[INFO] [11/20/2020 16:01:21.079] [main-1] [Cinnamon] Agent found Akka Cluster Sharding version: 2.6.8
[INFO] [11/20/2020 16:01:21.081] [main-1] [Cinnamon] Agent found Akka Cluster Sharding Typed version: 2.6.8
[INFO] [11/20/2020 16:01:21.082] [main-1] [Cinnamon] Agent found Akka Cluster version: 2.6.8
[INFO] [11/20/2020 16:01:21.121] [main-1] [Cinnamon] Agent found Akka Persistence version: 2.6.8
[INFO] [11/20/2020 16:01:21.127] [main-1] [Cinnamon] Agent found Akka Streams version: 2.6.8
[INFO] [11/20/2020 16:01:21.136] [main-1] [Cinnamon] Agent found Alpakka Kafka version: 1.1.0
[INFO] [11/20/2020 16:01:21.154] [main-1] [Cinnamon] Agent found Akka Actor Typed version: 2.6.8
```

To start the application with Lightbend Telemetry agent use the following commands for each service:

### `shopping-cart` service

```shell script
sbt "shopping-cart/test:runMain play.core.server.ProdServerStart"
```

### `inventory` service

It is similar to the `shopping-cart` service, but we need to specify a port to avoid conflict when both services are running (the default port is `9000`):

```shell script
sbt "inventory/test:runMain -Dplay.server.http.port=9001 play.core.server.ProdServerStart"
```

## Running tests

The project is also configured to attach the agent when running the tests. Here you can use the regular commands:

```shell script
sbt test
```

