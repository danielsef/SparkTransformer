name := "SparkTransformer"

version := "1.0"

scalaVersion := "2.10.6"


resolvers ++= Seq(
  "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
)

libraryDependencies ++= Seq(
  "com.streamsets" % "streamsets-datacollector-spark-api" % "3.5.2",
  "org.apache.spark" % "spark-core_2.11" % "2.1.0"
)