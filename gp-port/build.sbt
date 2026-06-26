organization := "io.pivotal.greenplum"
name         := "greenplum-connector-apache-spark"
version      := "2.2.0-spark4"
scalaVersion := "2.13.12"

// Spark 4.0 with Scala 2.13
val sparkVersion = "4.0.0"

libraryDependencies ++= Seq(
  // Spark - provided at runtime
  "org.apache.spark" %% "spark-sql"  % sparkVersion % "provided",
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",

  // Bundled dependencies (shaded into fat JAR)
  "org.postgresql"   % "postgresql"         % "42.7.3",
  "com.zaxxer"       % "HikariCP"           % "5.1.0",
  "org.eclipse.jetty" % "jetty-server"      % "11.0.24",
  "org.eclipse.jetty" % "jetty-servlet"     % "11.0.24",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback"   % "logback-classic"    % "1.4.14",
  "commons-io"       % "commons-io"         % "2.15.1",
  "commons-codec"    % "commons-codec"      % "1.17.1",
  "com.univocity"    % "univocity-parsers"  % "2.9.1",
  "org.apache.commons" % "commons-lang3"    % "3.14.0",
)

// sbt-assembly: merge strategy for fat JAR
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
  case PathList("META-INF", xs @ _*)              => MergeStrategy.discard
  case PathList("module-info.class")               => MergeStrategy.discard
  case PathList("reference.conf")                  => MergeStrategy.concat
  case x                                           => MergeStrategy.first
}

assembly / assemblyJarName := s"greenplum-connector-apache-spark-scala_2.13-2.2.0-spark4.jar"

// Exclude Spark from fat JAR
assembly / assemblyExcludedJars := {
  val cp = (assembly / fullClasspath).value
  cp filter { f =>
    val n = f.data.getName
    n.startsWith("spark-") || n.startsWith("scala-library") || n.startsWith("scala-reflect")
  }
}

// Service file for DataSourceRegister
Compile / resourceGenerators += Def.task {
  val dir = (Compile / resourceManaged).value
  val f   = dir / "META-INF" / "services" / "org.apache.spark.sql.sources.DataSourceRegister"
  IO.write(f, "io.pivotal.greenplum.spark.GreenplumRelationProvider\n")
  Seq(f)
}.taskValue
