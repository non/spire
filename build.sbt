import scala.language.existentials
import microsites._
import ReleaseTransformations._
import sbtcrossproject.CrossType
import sbtcrossproject.CrossPlugin.autoImport.crossProject

lazy val scalaCheckVersion = "1.15.4"

lazy val munit = "0.7.27"
lazy val munitDiscipline = "1.0.9"

lazy val shapelessVersion = "2.3.7"
lazy val algebraVersion = "2.2.3"

lazy val apfloatVersion = "1.10.0"
lazy val jscienceVersion = "4.3.1"
lazy val apacheCommonsMath3Version = "3.6.1"

val Scala213 = "2.13.6"

ThisBuild / crossScalaVersions := Seq(Scala213)
ThisBuild / scalaVersion := Scala213
ThisBuild / organization := "org.typelevel"

ThisBuild / githubWorkflowArtifactUpload := false

ThisBuild / githubWorkflowPublishTargetBranches := Seq()
ThisBuild / githubWorkflowJavaVersions := Seq("adopt@1.8", "adopt@1.11", "adopt@1.16")
ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep
    .Sbt(List("scalafmtCheckAll", "scalafmtSbtCheck"), name = Some("Check formatting")),
  WorkflowStep.Sbt(List("test:compile"), name = Some("Compile")),
  WorkflowStep.Sbt(List("test"), name = Some("Run tests")),
  WorkflowStep.Sbt(List("doc"), name = Some("Build docs"))
)

Global / onChangedBuildSource := ReloadOnSourceChanges
// Projects

lazy val spire = project
  .in(file("."))
  .settings(moduleName := "spire-root")
  .settings(spireSettings)
  .settings(unidocSettings)
  .settings(noPublishSettings)
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(spireJVM, spireJS)
  .dependsOn(spireJVM, spireJS)

lazy val spireJVM = project
  .in(file(".spireJVM"))
  .settings(moduleName := "spire-aggregate")
  .settings(spireSettings)
  .settings(unidocSettings)
  .settings(noPublishSettings)
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(macros.jvm,
             core.jvm,
             data.jvm,
             extras.jvm,
             examples,
             laws.jvm,
             legacy.jvm,
             platform.jvm,
             tests.jvm,
             util.jvm,
             benchmark
  )
  .dependsOn(macros.jvm,
             core.jvm,
             data.jvm,
             extras.jvm,
             examples,
             laws.jvm,
             legacy.jvm,
             platform.jvm,
             tests.jvm,
             util.jvm,
             benchmark
  )

lazy val spireJS = project
  .in(file(".spireJS"))
  .settings(moduleName := "spire-aggregate")
  .settings(spireSettings)
  .settings(unidocSettings)
  .settings(noPublishSettings)
  .enablePlugins(ScalaUnidocPlugin)
  .aggregate(macros.js, core.js, data.js, extras.js, laws.js, legacy.js, platform.js, tests.js, util.js)
  .dependsOn(macros.js, core.js, data.js, extras.js, laws.js, legacy.js, platform.js, tests.js, util.js)
  .enablePlugins(ScalaJSPlugin)

lazy val platform = crossProject(JSPlatform, JVMPlatform)
  .settings(moduleName := "spire-platform")
  .settings(spireSettings: _*)
  .settings(crossVersionSharedSources: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .dependsOn(macros, util)

lazy val macros = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "spire-macros")
  .settings(spireSettings: _*)
  .settings(scalaCheckSettings: _*)
  .settings(munitSettings: _*)
  .settings(crossVersionSharedSources: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)

lazy val data = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "spire-data")
  .settings(spireSettings: _*)
  .settings(crossVersionSharedSources: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)

lazy val legacy = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "spire-legacy")
  .settings(spireSettings: _*)
  .settings(crossVersionSharedSources: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)

lazy val util = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "spire-util")
  .settings(spireSettings: _*)
  .settings(crossVersionSharedSources: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .dependsOn(macros)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "spire")
  .settings(spireSettings: _*)
  .settings(coreSettings: _*)
  .settings(crossVersionSharedSources: _*)
  .enablePlugins(BuildInfoPlugin)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .dependsOn(macros, platform, util)

lazy val extras = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "spire-extras")
  .settings(spireSettings: _*)
  .settings(extrasSettings: _*)
  .settings(crossVersionSharedSources: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .dependsOn(macros, platform, util, core, data)

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(ScalaUnidocPlugin)
  .dependsOn(macros.jvm, core.jvm, extras.jvm)
  .settings(moduleName := "spire-docs")
  .settings(commonSettings: _*)
  .settings(spireSettings: _*)
  .settings(docSettings: _*)
  .settings(noPublishSettings)
  .enablePlugins(TutPlugin)
  .settings(commonJvmSettings: _*)

lazy val examples = project
  .settings(moduleName := "spire-examples")
  .settings(spireSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.chuusai" %% "shapeless" % shapelessVersion,
      "org.apfloat" % "apfloat" % apfloatVersion,
      "org.jscience" % "jscience" % jscienceVersion
    )
  )
  .settings(noPublishSettings)
  .settings(commonJvmSettings)
  .dependsOn(core.jvm, extras.jvm)

lazy val laws = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(moduleName := "spire-laws")
  .settings(spireSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "algebra-laws" % algebraVersion,
      "org.scalacheck" %%% "scalacheck" % scalaCheckVersion
    )
  )
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .dependsOn(core, extras)

lazy val tests = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(moduleName := "spire-tests")
  .settings(spireSettings: _*)
  .settings(munitSettings: _*)
  .settings(noPublishSettings: _*)
  .jvmSettings(commonJvmSettings: _*)
  .jsSettings(commonJsSettings: _*)
  .dependsOn(core, data, legacy, extras, laws)

lazy val benchmark: Project = project
  .in(file("benchmark"))
  .settings(moduleName := "spire-benchmark")
  .settings(spireSettings)
  .settings(noPublishSettings)
  .settings(commonJvmSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apfloat" % "apfloat" % apfloatVersion,
      "org.jscience" % "jscience" % jscienceVersion,
      "org.apache.commons" % "commons-math3" % apacheCommonsMath3Version
    )
  )
  .enablePlugins(JmhPlugin)
  .dependsOn(core.jvm, extras.jvm)

// General settings

addCommandAlias(
  "validateJVM",
  ";coreJVM/scalastyle;macrosJVM/test;coreJVM/test;extrasJVM/test;lawsJVM/test;testsJVM/test;examples/test;benchmark/test"
)

addCommandAlias("validateJS", ";macrosJS/test;coreJS/test;extrasJS/test;lawsJS/test;testsJS/test")

addCommandAlias("validate", ";validateJVM;validateJS")

lazy val buildSettings = Seq()

lazy val commonDeps = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "algebra" % algebraVersion
  )
)

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions.value.diff(
    Seq(
      "-Xfatal-warnings",
      "-language:existentials",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard"
    )
  ),
  resolvers += Resolver.sonatypeRepo("snapshots")
) ++ scalaMacroDependencies ++ warnUnusedImport

lazy val commonJsSettings = Seq(
  Global / scalaJSStage := FastOptStage,
  Test / parallelExecution := false,
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
)

lazy val commonJvmSettings = Seq()

lazy val docsMappingsAPIDir = settingKey[String]("Name of subdirectory in site target directory for api docs")

lazy val docSettings = Seq(
  Tut / scalacOptions := (Tut / scalacOptions).value.filterNot(Set("-Ywarn-unused-imports", "-Xlint").contains),
  micrositeName := "Spire",
  micrositeDescription := "Powerful new number types and numeric abstractions for Scala",
  micrositeAuthor := "Spire contributors",
  micrositeHighlightTheme := "atom-one-light",
  micrositeHomepage := "https://typelevel.org/spire",
  micrositeBaseUrl := "spire",
  micrositeDocumentationUrl := "/spire/api/spire/index.html",
  micrositeDocumentationLabelDescription := "API Documentation",
  micrositeExtraMdFiles := Map(
    file("AUTHORS.md") -> ExtraMdFileConfig(
      "authors.md",
      "home",
      Map("title" -> "Authors", "section" -> "Home", "position" -> "5")
    ),
    file("CHANGES.md") -> ExtraMdFileConfig(
      "changes.md",
      "home",
      Map("title" -> "Changes", "section" -> "Home", "position" -> "2")
    ),
    file("CONTRIBUTING.md") -> ExtraMdFileConfig(
      "contributing.md",
      "home",
      Map("title" -> "Contributing", "section" -> "Home", "position" -> "3")
    ),
    file("DESIGN.md") -> ExtraMdFileConfig(
      "design.md",
      "home",
      Map("title" -> "Design notes", "section" -> "Home", "position" -> "4")
    ),
    file("FRIENDS.md") -> ExtraMdFileConfig(
      "friends.md",
      "home",
      Map("title" -> "Friends of Spire", "section" -> "Home", "position" -> "6")
    )
  ),
  micrositeGithubOwner := "typelevel",
  micrositeGithubRepo := "spire",
  micrositePalette := Map(
    "brand-primary" -> "#5B5988",
    "brand-secondary" -> "#292E53",
    "brand-tertiary" -> "#222749",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
  ),
  micrositeConfigYaml := ConfigYml(
    yamlCustomProperties = Map(
      "spireVersion" -> version.value,
      "scalaVersion" -> scalaVersion.value
    )
  ),
  autoAPIMappings := true,
  ScalaUnidoc / unidoc / unidocProjectFilter :=
    inProjects(platform.jvm, macros.jvm, data.jvm, legacy.jvm, util.jvm, core.jvm, extras.jvm, laws.jvm),
  docsMappingsAPIDir := "api",
  addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, docsMappingsAPIDir),
  ghpagesNoJekyll := false,
  fork := true,
  javaOptions += "-Xmx4G", // to have enough memory in forks
//  fork in tut := true,
//  fork in (ScalaUnidoc, unidoc) := true,
  ScalaUnidoc / unidoc / scalacOptions ++= Seq(
    "-groups",
    "-doc-source-url",
    scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
    "-sourcepath",
    (LocalRootProject / baseDirectory).value.getAbsolutePath,
    "-diagrams"
  ),
  Tut / scalacOptions ~= (_.filterNot(Set("-Ywarn-unused-import", "-Ywarn-dead-code"))),
  git.remoteRepo := "git@github.com:typelevel/spire.git",
  makeSite / includeFilter := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md" | "*.svg",
  Jekyll / includeFilter := (makeSite / includeFilter).value
)

lazy val publishSettings = Seq(
  homepage := Some(url("https://typelevel.org/spire/")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  pomExtra := (
    <developers>
      <developer>
        <id>d_m</id>
        <name>Erik Osheim</name>
        <url>http://github.com/non/</url>
      </developer>
      <developer>
        <id>tixxit</id>
        <name>Tom Switzer</name>
        <url>http://github.com/tixxit/</url>
      </developer>
    </developers>
  )
) ++ credentialSettings ++ sharedPublishSettings ++ sharedReleaseProcess

lazy val scoverageSettings = Seq(
  coverageMinimumStmtTotal := 40,
  coverageFailOnMinimum := false,
  coverageHighlighting := true,
  coverageExcludedPackages := "spire\\.benchmark\\..*;spire\\.macros\\..*"
)

lazy val coreSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](version, scalaVersion),
  buildInfoPackage := "spire",
  Compile / sourceGenerators += (Compile / genProductTypes).taskValue,
  genProductTypes := {
    val scalaSource = (Compile / sourceManaged).value
    val s = streams.value
    s.log.info("Generating spire/std/tuples.scala")
    val algebraSource = ProductTypes.algebraProductTypes
    val algebraFile = (scalaSource / "spire" / "std" / "tuples.scala").asFile
    IO.write(algebraFile, algebraSource)

    Seq[File](algebraFile)
  }
)

lazy val extrasSettings = Seq(
//  sourceGenerators in Compile <+= buildInfo,
//  buildInfoKeys := Seq[BuildInfoKey](version, scalaVersion),
//  buildInfoPackage := "spire.extras"
)

lazy val genProductTypes = TaskKey[Seq[File]]("gen-product-types", "Generates several type classes for Tuple2-22.")

lazy val scalaCheckSettings = Seq(libraryDependencies += "org.scalacheck" %%% "scalacheck" % scalaCheckVersion % Test)

lazy val munitSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % munit % Test,
    "org.typelevel" %%% "discipline-munit" % munitDiscipline % Test
  )
)

lazy val spireSettings = buildSettings ++ commonSettings ++ commonDeps ++ publishSettings ++ scoverageSettings

lazy val unidocSettings = Seq(
  ScalaUnidoc / unidoc / unidocProjectFilter := inAnyProject -- inProjects(examples, benchmark, tests.jvm)
)

////////////////////////////////////////////////////////////////////////////////////////////////////
// Base Build Settings - Should not need to edit below this line.
// These settings could also come from another file or a plugin.
// The only issue if coming from a plugin is that the Macro lib versions
// are hard coded, so an overided facility would be required.

addCommandAlias("gitSnapshots", ";set version in ThisBuild := git.gitDescribedVersion.value.get + \"-SNAPSHOT\"")

lazy val noPublishSettings = Seq(
  publish := (()),
  publishLocal := (()),
  publishArtifact := false
)

lazy val crossVersionSharedSources: Seq[Setting[_]] =
  Seq(Compile, Test).map { sc =>
    (sc / unmanagedSourceDirectories) ++= {
      (sc / unmanagedSourceDirectories).value.map { dir: File =>
        CrossVersion.partialVersion(scalaBinaryVersion.value) match {
          case Some((major, minor)) =>
            new File(s"${dir.getPath}_$major.$minor")
          case None =>
            sys.error("couldn't parse scalaBinaryVersion ${scalaBinaryVersion.value}")
        }
      }
    }
  }

lazy val scalaMacroDependencies: Seq[Setting[_]] = Seq(
  libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value % "provided"
)

lazy val commonScalacOptions = Def.setting(
  (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v >= 13 =>
      Seq()
    case _ =>
      Seq(
        "-Yno-adapted-args",
        "-Xfuture"
      )
  }) ++ Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )
)

lazy val sharedPublishSettings = Seq(
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle := true,
  Test / publishArtifact := false,
  pomIncludeRepository := Function.const(false),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("Snapshots".at(nexus + "content/repositories/snapshots"))
    else
      Some("Releases".at(nexus + "service/local/staging/deploy/maven2"))
  }
)

lazy val sharedReleaseProcess = Seq(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  )
)

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if (n >= 11) && (n <= 12) =>
        Seq("-Ywarn-unused-import")
      case _ => Seq()
    }
  },
  Compile / console / scalacOptions ~= { _.filterNot("-Ywarn-unused-import" == _) },
  Test / console / scalacOptions := (Compile / console / scalacOptions).value
)

// For Travis CI - see http://www.cakesolutions.net/teamblogs/publishing-artefacts-to-oss-sonatype-nexus-using-sbt-and-travis-ci
lazy val credentialSettings = Seq(
  credentials ++= (for {
    username <- Option(System.getenv().get("SONATYPE_USERNAME"))
    password <- Option(System.getenv().get("SONATYPE_PASSWORD"))
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", username, password)).toSeq,
  credentials += Credentials(
    Option(System.getProperty("build.publish.credentials"))
      .map(new File(_))
      .getOrElse(Path.userHome / ".ivy2" / ".credentials")
  )
)
