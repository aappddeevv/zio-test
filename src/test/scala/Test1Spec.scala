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
    extends ZIOTestSpec
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
    val ioa = backendClient.fetch(HttpRequest("GET", "/blah", Map(), "")) { r =>
      IO.now(r.body shouldBe "")
    }
    ioa.toTestFuture
  }

  it should "fail1" in {
    Future.failed(new RuntimeException("blah"))
  }

  it should "fail2" in {
    Future.successful(fail)
  }

}
