credentials in ThisBuild += Credentials(Path.userHome / ".lightbend" / "commercial.credentials")
resolvers in ThisBuild += "lightbend-commercial-maven" at "https://repo.lightbend.com/commercial-releases"
