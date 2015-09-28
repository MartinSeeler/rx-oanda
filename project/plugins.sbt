libraryDependencies += "org.slf4j" % "slf4j-nop" % "1.7.10"
resolvers += Resolver.url(
  "tut-plugin",
  url("http://dl.bintray.com/content/tpolecat/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages"    % "0.5.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-git"        % "0.8.5")
addSbtPlugin("de.knutwalker"    % "sbt-knutwalker" % "0.2.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-site"       % "0.8.1")
addSbtPlugin("org.tpolecat"     % "tut-plugin"     % "0.4.0")
