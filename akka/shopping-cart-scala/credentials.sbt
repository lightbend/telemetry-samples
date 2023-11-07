ThisBuild / resolvers ++= {
  sys.env.get("LIGHTBEND_COMMERCIAL_MVN").toSeq.map { url =>
    "lightbend-commercial-mvn" at url
  }
}
