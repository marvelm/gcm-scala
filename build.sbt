name := "gcm-scala"

version := "0.0.1"

organization := "ca.marvelmathew"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "releases" at "https://oss.sonatype.org/content/repositories/releases",
  "spray repo" at "http://repo.spray.io"
)

scalacOptions ++= Seq("-deprecation", "-unchecked")

scalariformSettings

libraryDependencies ++= {
  val sprayVersion = "1.3.3"
  Seq(
    "org.specs2"        %% "specs2"             % "2.3.12"           % "test",
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-client" % sprayVersion,
    "io.spray" %% "spray-httpx" % sprayVersion,
    "io.spray" %%  "spray-json" % "1.3.2",
    "com.typesafe.akka" %% "akka-actor" % "2.3.9",
    "com.typesafe.slick" %% "slick" % "3.0.2",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "com.zaxxer" % "HikariCP" % "2.3.9",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
    "io.netty" % "netty-all" % "4.1.0.Beta6",
    "org.igniterealtime.smack" % "smack-core" % "4.1.3",
    "org.igniterealtime.smack" % "smack-extensions" % "4.1.3",
    "org.igniterealtime.smack" % "smack-tcp" % "4.1.3",
    "org.igniterealtime.smack" % "smack-java7" % "4.1.3",
    "org.igniterealtime.smack" % "smack-im" % "4.1.3",
    "org.igniterealtime.smack" % "smack-debug" % "4.1.3"
  )
}
