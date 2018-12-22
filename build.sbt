
libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-zio" % "0.5.1",
  "org.scalaz" %% "scalaz-zio-interop-future" % "0.5.1",
  "org.scalaz" %% "scalaz-zio-interop-cats" % "0.5.1",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "org.specs2" %% "specs2-core" % "4.3.6" % "test"
)

scalacOptions ++= Seq(
  "-language:_",
  "-Xcheckinit",
  "-explaintypes",
  "-deprecation",
  "-Ypartial-unification",
  "-Yno-adapted-args",
  "-Ywarn-value-discard",
  "-Ywarn-infer-any")

scalacOptions in Test ++= Seq("-Yrangepos")
