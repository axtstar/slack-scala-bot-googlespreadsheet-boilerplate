
lazy val root = (project in file(".")).
  settings(
    name := "slackScalaSample",
    version := "0.0.1",
    scalaVersion := "2.11.8"
  )

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

libraryDependencies ++= Seq(
  "com.github.gilbertw1" %% "slack-scala-client" % "0.1.8",
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.scala-lang" % "scala-actors" % "2.11.8",
  "com.google.api-client" % "google-api-client" % "1.22.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0",
  "com.google.apis" % "google-api-services-drive" % "v3-rev52-1.22.0",
  "com.google.apis" % "google-api-services-sheets" % "v4-rev38-1.22.0"
)
