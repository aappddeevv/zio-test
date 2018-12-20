package ziotest

import scala.concurrent._
import ExecutionContext.Implicits.global

import org.scalatest._
import scalaz.zio._
import scalaz.zio.interop.future._

import odata._
import http._
import backend._

trait ZIOTestSpec {
  val rts = new RTS { }

  implicit class ToFuture(io: IO[Nothing,Assertion]) {
    def toTestFuture: Future[Assertion] =
      rts.unsafeRun(io.toFuture)
  }
}
