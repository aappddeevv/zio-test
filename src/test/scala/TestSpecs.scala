package ziotest

import scala.concurrent._

import org.specs2._
import scalaz.zio._
import scalaz.zio.internal._

abstract class AbstractRTSSpec
    extends Specification
    with RTS {

  override lazy val env =
    Env.newDefaultEnv{ cause =>
      IO.sync(println(s"ZIOTestSpec reportFailure: with cause $cause"))
    }
}

class TestSpecs extends AbstractRTSSpec {
  import Test1._

  def is = s2"""

This is my first specification
  it is working           $e1
  really working!         $e2
"""

  def e1 = 1 must_== 1
  def e2 = Array(1,2,3) must_== Array(1,2,3)

  // "backend" should "do a simple response" in {
  //   val ioa = backendClient.fetch(good){ r =>
  //     IO.now(r.body shouldBe "")
  //   }
  //   ioa.toTestFuture()
  // }

  // it should "fail on an unanswerable request" in {
  //   val ioa = backendClient.fetch(notInAnswers) { r =>
  //     IO.now(r.body shouldBe "")
  //   }
  //   ioa.toTestFuture()
  // }
}
