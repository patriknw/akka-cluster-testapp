import sbt._
import sbt.Keys._
import akka.sbt.AkkaKernelPlugin
import akka.sbt.AkkaKernelPlugin.{ Dist, outputDirectory, distJvmOptions, additionalLibs}

object AkkaClusterTestappBuild extends Build {

  val akkaVersion = "2.2-20130427-001506"

  lazy val akkaOpsworks = Project(
    id = "akka-cluster-testapp",
    base = file("."),
    settings = Project.defaultSettings ++ AkkaKernelPlugin.distSettings ++ Seq(
      name := "akka-cluster-testapp",
      organization := "com.typesafe.akka",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.10.1",
      scalacOptions in Compile ++= Seq("-encoding", "UTF-8", "-target:jvm-1.6", "-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
      javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:unchecked", "-Xlint:deprecation"),
      // this is only needed while we use timestamped snapshot version of akka
      resolvers += "Typesafe Snapshot Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
      // this is only needed while we use the ScalaBuff SNAPSHOT version
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-cluster-experimental" % akkaVersion,
        "com.typesafe.akka" %% "akka-contrib" % akkaVersion,
        "com.typesafe.akka" %% "akka-kernel" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
        "ch.qos.logback" % "logback-classic" % "1.0.7",
        "org.fusesource" % "sigar" % "1.6.4",
        "com.amazonaws" % "aws-java-sdk" % "1.4.2.1"),
      mainClass in (Compile, run) := Some("testapp.Main"),
      distJvmOptions in Dist := "-Xms256M -Xmx1024M -XX:-HeapDumpOnOutOfMemoryError",
      additionalLibs in Dist := file("sigar").listFiles.filter(f => !f.isDirectory)
    )
  )
}
