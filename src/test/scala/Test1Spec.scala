package ziotest

import scala.concurrent._
import ExecutionContext.Implicits.global

import org.scalatest._
import scalaz.zio._
import scalaz.zio.interop.future._

import odata._
import http._
import backend._

class Test1Spec
    extends AsyncFlatSpec
    with OptionValues
    with Matchers
    with ZIOTestSpec {

  "zio" should "do something" in {
    IO.now(10 shouldBe 10).toTestFuture
  }

}
