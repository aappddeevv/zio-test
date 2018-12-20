package ziotest

import scala.concurrent._
import ExecutionContext.Implicits.global

import org.scalatest._
import scalaz.zio._
import scalaz.zio.interop.future._

import odata._
import http._
import backend._

class ZIOTestSpec extends AsyncFlatSpec {
  val rts = new RTS { }

  implicit class ToFuture[E](io: IO[E, Assertion]) {
    def toTestFuture: Future[Assertion] =
      rts.unsafeRun(io.redeemPure(
        _ => fail,
        identity
      ).toFuture)
  }
}
