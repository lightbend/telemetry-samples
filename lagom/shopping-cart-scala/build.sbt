import com.lightbend.lagom.core.LagomVersion

organization in ThisBuild := "com.example"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.3"

val postgresDriver             = "org.postgresql"               % "postgresql"                                     % "42.2.18"
val macwire                    = "com.softwaremill.macwire"     %% "macros"                                        % "2.3.7" % "provided"
val scalaTest                  = "org.scalatest"                %% "scalatest"                                     % "3.2.2" % Test
val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api"                 % "1.0.9"
val lagomScaladslAkkaDiscovery = "com.lightbend.lagom"          %% "lagom-scaladsl-akka-discovery-service-locator" % LagomVersion.current

ThisBuild / scalacOptions ++= List("-encoding", "utf8", "-deprecation", "-feature", "-unchecked", "-Xfatal-warnings")

def dockerSettings = Seq(
  dockerUpdateLatest := true,
  dockerBaseImage := getDockerBaseImage(),
  dockerUsername := sys.props.get("docker.username"),
  dockerRepository := sys.props.get("docker.registry")
)

def getDockerBaseImage(): String = sys.props.get("java.version") match {
  case Some(v) if v.startsWith("11") => "adoptopenjdk/openjdk11"
  case _ => "adoptopenjdk/openjdk8"
}

// Lightbend Telemetry basic configuration. See more at:
// https://developer.lightbend.com/docs/telemetry/current/getting-started/lagom_scala.html
val cinnamonSettings = Seq(
  cinnamon in test := false,
  libraryDependencies ++= Seq(
    // Use Coda Hale Metrics and Lagom instrumentation
    Cinnamon.library.cinnamonCHMetrics,
    Cinnamon.library.cinnamonLagom,
    Cinnamon.library.cinnamonLagomProjection,
    // Uncomment if you are using the Prometheus backend.
    // See https://developer.lightbend.com/docs/telemetry/current/sandbox/prometheus-sandbox.html
    //  Cinnamon.library.cinnamonPrometheus,
    //  Cinnamon.library.cinnamonPrometheusHttpServer
  )
)

// Update the version generated by sbt-dynver to remove any + characters, since these are illegal in docker tags
version in ThisBuild ~= (_.replace('+', '-'))
dynver in ThisBuild ~= (_.replace('+', '-'))

lazy val `shopping-cart-scala` = (project in file("."))
  .aggregate(`shopping-cart-api`, `shopping-cart`, `inventory-api`, inventory)

lazy val `shopping-cart-api` = (project in file("shopping-cart-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `shopping-cart` = (project in file("shopping-cart"))
  .enablePlugins(LagomScala, Cinnamon)
  .settings(
    cinnamonSettings,
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceJdbc,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      postgresDriver,
      lagomScaladslAkkaDiscovery,
      akkaDiscoveryKubernetesApi
    )
  )
  .settings(dockerSettings)
  .settings(lagomForkedTestSettings)
  .dependsOn(`shopping-cart-api`)

lazy val `inventory-api` = (project in file("inventory-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val inventory = (project in file("inventory"))
  .enablePlugins(LagomScala, Cinnamon)
  .settings(
    cinnamonSettings,
    libraryDependencies ++= Seq(
      lagomScaladslKafkaClient,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      lagomScaladslAkkaDiscovery
    )
  )
  .settings(dockerSettings)
  .dependsOn(`inventory-api`, `shopping-cart-api`)

lagomCassandraEnabled in ThisBuild := false
