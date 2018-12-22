package ziotest

import scala.concurrent._

import org.scalatest._
import scalaz.zio._
import scalaz.zio.interop.future._

import odata._
import http._
import backend._

import ttg.scalaz.zio._

class Test1Spec
    extends AbstractZIOTestSpec
    with OptionValues
    with Matchers {

  import Test1._

  "backend" should "do a simple response" in {
    val ioa = backendClient.fetch(good){ r =>
      IO.now(r.body shouldBe "")
    }
    ioa.toTestFuture
  }

  it should "fail on an unanswerable request" in {
    val ioa = backendClient.fetch(notInAnswers) { r =>
      IO.now(r.body shouldBe "")
    }
    ioa.expectFailure
  }

  it should "expect error if not 200 in expectOr" in {
    val ioa = backendClient2.expectOr[String](bad, decoders.passthrough _)(
      (s, resp) => IO.fail(ClientError(s, None))
    ).flatMap(bodystr => IO.now(bodystr shouldBe ""))
    ioa.expectFailure(ExitResult.Cause.checked(ClientError("bad status", None)))
  }

}
