name := "shopping-cart-service"
version := "1.0"

organization := "com.lightbend.akka.samples"
organizationHomepage := Some(url("https://akka.io"))
licenses := Seq(("CC0", url("https://creativecommons.org/publicdomain/zero/1.0")))

scalaVersion := "2.12.12"

Compile / scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint")
Compile / javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

// We are now forking the process because of Cinnamon, so we need to
// passthrough the config.resource to the forked process.
javaOptions ++= sys.props.get("config.resource").map(r => s"-Dconfig.resource=$r")

Test / parallelExecution := false
Test / testOptions += Tests.Argument("-oDF")
Test / logBuffered := false

Global / cancelable := false // ctrl-c

val AkkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.1"
val AkkaManagementVersion = "1.0.9"
val AkkaPersistenceCassandraVersion = "1.0.4"
val AlpakkaKafkaVersion = "2.0.5"
val AkkaProjectionVersion = "1.0.0"
val GatlingVersion = "3.4.1"

enablePlugins(AkkaGrpcPlugin, Cinnamon, GatlingPlugin)

libraryDependencies ++= Seq(
  // 1. Basic dependencies for a clustered application
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,

  // Akka Management powers Health Checks and Akka Cluster Bootstrapping
  "com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,

  // Common dependencies for logging and testing
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.1.2" % Test,

  // 2. Using gRPC and/or protobuf
  "com.typesafe.akka" %% "akka-http2-support" % AkkaHttpVersion,

  // 3. Using Akka Persistence
  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % AkkaPersistenceCassandraVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test,

  // 4. Querying or projecting data from Akka Persistence
  "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
  "com.lightbend.akka" %% "akka-projection-eventsourced" % AkkaProjectionVersion,
  "com.lightbend.akka" %% "akka-projection-cassandra" % AkkaProjectionVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKafkaVersion,
  "com.lightbend.akka" %% "akka-projection-testkit" % AkkaProjectionVersion % Test,
)

cinnamon in run := true
cinnamon in test := true
cinnamonLogLevel := "INFO"

libraryDependencies ++= Seq(
  // Use Coda Hale Metrics
  Cinnamon.library.cinnamonCHMetrics,
  // Use Akka instrumentation
  Cinnamon.library.cinnamonAkka,
  Cinnamon.library.cinnamonAkkaTyped,
  Cinnamon.library.cinnamonAkkaPersistence,
  Cinnamon.library.cinnamonAkkaStream,
  // Use Akka HTTP instrumentation
  Cinnamon.library.cinnamonAkkaHttp,
  // Use Akka Projection Instrumentation
  Cinnamon.library.cinnamonAkkaProjection,
  Cinnamon.library.cinnamonPrometheus,
  Cinnamon.library.cinnamonPrometheusHttpServer
)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % GatlingVersion % Test,
  "io.gatling"            % "gatling-test-framework"    % GatlingVersion % Test,
  "com.github.phisgr"    %% "gatling-grpc"              % "0.10.1"       % Test,
)

// To avoid a dependency conflict when running gatling tests
val JacksonVersion = "2.10.5"
dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-annotations" % JacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVersion,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % JacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % JacksonVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % JacksonVersion,
  "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % JacksonVersion,
  "com.fasterxml.jackson.module" % "jackson-module-paranamer" % JacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
)