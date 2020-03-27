// The Lagom plugin
addSbtPlugin("com.lightbend.lagom" % "lagom-sbt-plugin" % "1.6.0+67-20200326-2130-telemetry")

// Set the version dynamically to the git hash
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "3.3.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.2.0")
