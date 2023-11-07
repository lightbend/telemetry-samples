name := "shopping-cart-service"

organization := "com.lightbend.akka.samples"
organizationHomepage := Some(url("https://akka.io"))
licenses := Seq(("CC0", url("https://creativecommons.org/publicdomain/zero/1.0")))

scalaVersion := "2.13.5"

Compile / scalacOptions ++= Seq(
  "-target:11",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlog-reflective-calls",
  "-Xlint")

Compile / javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

Test / parallelExecution := false
Test / testOptions += Tests.Argument("-oDF")
Test / logBuffered := false

run / fork := true
// pass along config selection to forked jvm
run / javaOptions ++= sys.props
  .get("config.resource")
  .fold(Seq.empty[String])(res => Seq(s"-Dconfig.resource=$res"))

Global / cancelable := false // ctrl-c

val AkkaVersion = "2.8.5"
val AkkaHttpVersion = "10.5.3"
val AkkaManagementVersion = "1.4.1"
val AkkaPersistenceJdbcVersion = "5.2.1"
val AlpakkaKafkaVersion = "4.0.2"
val AkkaProjectionVersion = "1.4.0"
val AkkaDiagnosticsVersion = "2.0.1"
val ScalikeJdbcVersion = "3.5.0"

enablePlugins(AkkaGrpcPlugin, JavaAppPackaging, DockerPlugin)
enablePlugins(Cinnamon, GatlingPlugin)

dockerBaseImage := "docker.io/library/eclipse-temurin:17.0.3_7-jre-jammy"
dockerUsername := sys.props.get("docker.username")
dockerRepository := sys.props.get("docker.registry")
dockerUpdateLatest := true

ThisBuild / dynverSeparator := "-"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

libraryDependencies ++= Seq(
  // 1. Basic dependencies for a clustered application
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
  // Akka Management powers Health Checks, Akka Cluster Bootstrapping, and Akka Diagnostics
  "com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % AkkaManagementVersion,
  "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
  "com.lightbend.akka" %% "akka-diagnostics" % AkkaDiagnosticsVersion,
  // Common dependencies for logging and testing
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.11",
  "org.scalatest" %% "scalatest" % "3.1.2" % Test,
  // 2. Using gRPC and/or protobuf
  "com.typesafe.akka" %% "akka-http2-support" % AkkaHttpVersion,
  // 3. Using Akka Persistence
  "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.lightbend.akka" %% "akka-persistence-jdbc" % AkkaPersistenceJdbcVersion,
  "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % Test,
  "org.postgresql" % "postgresql" % "42.2.18",
  // 4. Querying or projecting data from Akka Persistence
  "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
  "com.lightbend.akka" %% "akka-projection-eventsourced" % AkkaProjectionVersion,
  "com.lightbend.akka" %% "akka-projection-jdbc" % AkkaProjectionVersion,
  "org.scalikejdbc" %% "scalikejdbc" % ScalikeJdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % ScalikeJdbcVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKafkaVersion,
  "com.lightbend.akka" %% "akka-projection-testkit" % AkkaProjectionVersion % Test
)

run / cinnamon := true
test / cinnamon := true
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

// Below are settings required by Gatling:

import scalapb.compiler.Version.{ scalapbVersion => ScalaPbVersion }
val GatlingVersion = "3.5.1"
val GatlingGrpcVersion = "0.11.1"

// This allow the generation of gRPC's native method descriptors
// that can be consumed in tests
akkaGrpcCodeGeneratorSettings += "grpc"

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc"      % ScalaPbVersion,
  "io.gatling.highcharts" % "gatling-charts-highcharts" % GatlingVersion     % Test,
  "io.gatling"            % "gatling-test-framework"    % GatlingVersion     % Test,
  "com.github.phisgr"     % "gatling-grpc"              % GatlingGrpcVersion % Test,
)

// To avoid a dependency conflict when running gatling tests
val JacksonVersion = "2.11.4"
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
