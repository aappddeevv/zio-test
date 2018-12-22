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

  "backend" should "0: do a simple response" in {
    val ioa = backendClient0.fetch(good){ r =>
      IO.now(r.body shouldBe "")
    }
    ioa.toTestFuture
  }

  it should "0: fail on an unanswerable request" in {
    val ioa = backendClient0.fetch(notInAnswers) { r =>
      IO.now(r.body shouldBe "")
    }
    ioa.expectFailure(
      ExitResult.Cause.checked(BackendError(Backend.message))
    )
  }

  it should "2: expect error if not 200 in expectOr" in {
    val ioa = backendClient2.expectOr[String](bad, decoders.passthrough _)(
      (s, resp) => IO.fail(ClientError(s))
    ).flatMap(bodystr => IO.now(bodystr shouldBe ""))
    ioa.expectFailure(ExitResult.Cause.checked(ClientError("bad status")))
  }

  it should "3: expect error if not 200 in expectOr" in {
    val ioa = backendClient3.expectOr[String](bad, decoders.passthrough _)(
      (s, resp) => IO.fail(ClientError(s))
    ).flatMap(bodystr => IO.now(bodystr shouldBe ""))
    ioa.expectFailure(ExitResult.Cause.checked(ClientError("bad status")))
  }

  it should "3: expect backend error in expectOr" in {
    val ioa = backendClient3.expectOr[String](notInAnswers, decoders.passthrough _)(
      (s, resp) => IO.fail(ClientError(s))
    ).flatMap(bodystr => IO.now(bodystr shouldBe ""))
    ioa.expectFailure(ExitResult.Cause.checked(ClientBackendError(Backend.message)))
  }


}
