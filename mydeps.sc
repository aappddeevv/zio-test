import $ivy.`org.scalaz::scalaz-zio:0.5.1`
import $ivy.`org.typelevel::cats-core:1.5.0`
import $ivy.`org.typelevel::cats-effect:1.1.0`
import $ivy.`com.github.pathikrit::better-files:3.4.0`
import concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import cats._
import cats.data._
import cats.implicits._
//import cats.effect._
//implicit val timer = IO.timer(scala.concurrent.ExecutionContext.Implicits.global)
//implicit val cs = IO.contextShift(global)
import scalaz.zio._

