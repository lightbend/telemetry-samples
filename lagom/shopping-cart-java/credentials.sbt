resolvers in ThisBuild ++= {
    val mvnResolver = sys.env.get("LIGHTBEND_COMMERCIAL_MVN").map { url =>
        "lightbend-commercial-mvn" at url
    }

    val ivyResolver = sys.env.get("LIGHTBEND_COMMERCIAL_IVY").map { u =>
        Resolver.url("lightbend-commercial-ivy", url(u))(Resolver.ivyStylePatterns)
    }

    (mvnResolver ++ ivyResolver).toList
}
