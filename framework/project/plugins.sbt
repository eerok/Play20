logLevel := Level.Warn

resolvers += Classpaths.typesafeResolver

resolvers += ScalaToolsSnapshots

resolvers += Resolver.url("sbt-plugin-releases", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbtscalariform" % "sbtscalariform" % "0.3.1")

addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "latest.milestone")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.1.3")
