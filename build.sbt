import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtSite.SiteKeys._
import sbtrelease.ReleaseStateTransformations._

name := "rx-oanda"
scalaVersion := "2.11.7"

val akkaStreamV = "1.0"
val akkaHttpV = "1.0"

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= List(
  "com.typesafe.akka" %% "akka-stream-experimental"          % akkaStreamV,
  "com.typesafe.akka" %% "akka-http-experimental"            % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaHttpV,

  "io.circe" %% "circe-core" % "0.2.0-SNAPSHOT",
  "io.circe" %% "circe-generic" % "0.2.0-SNAPSHOT",
  "io.circe" %% "circe-jawn" % "0.2.0-SNAPSHOT",

  "com.typesafe.akka" %% "akka-stream-testkit-experimental"  % akkaStreamV  % "test",
  "com.typesafe.akka" %% "akka-http-testkit-experimental"    % akkaHttpV    % "test"
)

organization := "io.martinseeler"
startYear := Some(2015)
maintainer := "Martin Seeler"
githubProject := Github("MartinSeeler", "rx-oanda")
description := "Oanda API wrapper implemented with akka-streams and akka-http."
javaVersion := JavaVersion.Java17

autoAPIMappings := true
pomExtra := pomExtra.value ++
  <properties>
    <info.apiURL>http://{githubProject.value.org}.github.io/{githubProject.value.repo}/api/{version.value}/</info.apiURL>
  </properties>
site.settings
ghpages.settings
tutSettings
tutSourceDirectory := sourceDirectory.value / "tut"
site.includeScaladoc("api")
site.addMappingsToSiteDir(tut, "tut")
ghpagesNoJekyll := false
scalacOptions in doc ++= Seq(
  "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
  "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
  "-doc-title", githubProject.value.repo,
  "-doc-version", version.value,
  "-diagrams",
  "-groups"
)
git.remoteRepo := s"git@github.com:${githubProject.value.org}/${githubProject.value.repo}.git"
includeFilter in makeSite ~= (_ || "*.yml" || "*.md" || "*.scss")
tutScalacOptions ~= (_.filterNot(Set("-Xfatal-warnings", "-Ywarn-unused-import", "-Ywarn-dead-code")))
watchSources <++= (tutSourceDirectory, siteSourceDirectory, includeFilter in makeSite) map { (t, s, f) ⇒ (t ** "*.md").get ++ (s ** f).get }

releaseProcess := List[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishSignedArtifacts,
  releaseToCentral,
  pushGithubPages,
  setNextVersion,
  commitNextVersion
)

lazy val publishSignedArtifacts = ReleaseStep(
  action = Command.process("publishSigned", _),
  enableCrossBuild = true
)

lazy val releaseToCentral = ReleaseStep(
  action = Command.process("sonatypeReleaseAll", _),
  enableCrossBuild = true
)

lazy val pushGithubPages = ReleaseStep(
  action = Command.process("ghpagesPushSite", _),
  enableCrossBuild = false
)

addCommandAlias("travis", ";clean;coverage;test;coverageReport;coverageAggregate")
