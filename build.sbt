import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import com.typesafe.sbt.SbtSite.SiteKeys._
import sbtrelease.ReleaseStateTransformations._

name := "rx-oanda"
scalaVersion := "2.11.8"

val akkaStreamV = "2.4.8"
val akkaHttpV = "2.4.8"
val circeV = "0.4.1"
val streamCirceV = "3.0.0"
val scalaTestV = "2.2.6"
val scalaCheckV = "1.12.5"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= List(
  // Akka Streams and Akka Http
  "com.typesafe.akka" %% "akka-stream"                       % akkaStreamV,
  "com.typesafe.akka" %% "akka-stream-testkit"               % akkaStreamV  % "test",
  "com.typesafe.akka" %% "akka-http-experimental"            % akkaHttpV,
  "com.typesafe.akka" %% "akka-http-testkit"                 % akkaHttpV    % "test",
  "de.knutwalker"     %% "akka-stream-circe"                 % streamCirceV,
  // circe for decoding
  "io.circe"          %% "circe-core"                        % circeV,
  "io.circe"          %% "circe-generic"                     % circeV,
  "io.circe"          %% "circe-parser"                      % circeV,
  // testing
  "org.scalatest"     %% "scalatest"                         % scalaTestV   % "test",
  "org.scalacheck"    %% "scalacheck"                        % scalaCheckV  % "test"
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
