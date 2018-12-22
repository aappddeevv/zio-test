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

  it should "3: expect ClientError for unexpected status" in {
    val ioa = backendClient3.expectOr[String](bad, decoders.passthrough _)(
      (s, resp) => IO.fail(ClientError(s))
    ).flatMap(bodystr => IO.now(bodystr shouldBe ""))
    ioa.expectFailure(ExitResult.Cause.checked(ClientError("bad status")))
  }

  it should "3: expect ClientBackendError for comm failure" in {
    val ioa = backendClient3.expectOr[String](notInAnswers, decoders.passthrough _)(
      (s, resp) => IO.fail(ClientError(s))
    ).flatMap(bodystr => IO.now(bodystr shouldBe ""))
    ioa.expectFailure(ExitResult.Cause.checked(ClientBackendError(Backend.message)))
  }

  it should "3: be clear that an effect does not have a hidden failure in the effect" in {
    val ioa: IO[ClientErrorBase, Assertion] =
      backendClient3.expectOr[String](notInAnswers, decoders.passthrough _)(
        (s, resp) => IO.fail(ClientError(s))
      ).flatMap(bodystr => IO.now(bodystr shouldBe ""))

    // Let's assume we catch all errors now...and we don't want to guess whether
    // there is a Throwable in there but not known. This mimics the final layer
    // in a UI. Normally, we would map the result into a UI presentable value or
    // a user level error message on the left.
    val ioa2: IO[Option[String], Assertion] = ioa.catchAll {
      case ClientBackendError(message) => IO.fail(Option(message))
      case ClientError(message) => IO.fail(Option(message))
    }
    // Normally we would dhave add an IO[Assertion] and would not know if errors
    // had been handled or not. Even if it was IO[Either[E, Assertion]] the IO
    // could still have an error and we would have to check. Assuming the types
    // do not lie, I would be much more confident that the error handling is
    // complete in ioa2 above.
 
    // if E or A update the UI as needed
    // ...

    ioa2.expectFailure
  }

}
