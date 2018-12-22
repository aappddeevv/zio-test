package ttg.scalaz.zio

import scala.concurrent._
import scala.util.Try

import org.scalatest._
import scalaz.zio._
import scalaz.zio.internal._
import scalaz.zio.interop.future._

abstract class AbstractZIOTestSpec
    extends AsyncFlatSpec
    with RTS {

  // used to allow pattern matching
  private[ttg] case class ZIOTestException[E](c: ExitResult.Cause[E])
      extends RuntimeException(c.toString())

  override lazy val env =
    Env.newDefaultEnv{ cause =>
      IO.sync(println(s"ZIOTestSpec reportFailure: with cause $cause"))
    }

  implicit class ToFuture[E](io: IO[E, Assertion]) {
    private[ttg] def toTestFuture2: Future[Assertion] = {
      // if we were using sync vs async, we could just use the following line and e in ZIOTestSpecFailure is E
      //unsafeRun(io.toFutureE(e => new RuntimeException(e.toString)))
      val p = scala.concurrent.Promise[Assertion]()
      unsafeRunAsync(io){ // or _.fold(...)
        case ExitResult.Failed(c) => p.failure(ZIOTestException(c))
        case ExitResult.Succeeded(a) => p.complete(Try(a))
      }
      p.future
    }
    // push Cause[E] into a TestFailedException (which is scalatest private)
    def toTestFuture: Future[Assertion] = {
      val p = scala.concurrent.Promise[Assertion]()
      unsafeRunAsync(io){
        case ExitResult.Failed(e) => p.complete(Try(fail(e.toString)))
        case ExitResult.Succeeded(a) => p.complete(Try(a))
      }
      p.future
    }

  }

  // use with ToFuture.toTestFailure2
  private[ttg] implicit class RichFuture(f: Future[Assertion]) {
    // flip a ZIOTestException to Succeeded.
    def expectFailure: Future[Assertion] =
      f.recover {
        case ZIOTestException(c) => Succeeded
      }
    def expectFailure[E](e: ExitResult.Cause[E]): Future[Assertion] =
      f.recover {
        case ZIOTestException(c) if e == c => Succeeded
      }    
  }

  implicit class RichIO[E](io: IO[E, Assertion]) {
    def expectFailure: Future[Assertion] =
      io.toTestFuture2.expectFailure
    def expectFailure(c: ExitResult.Cause[E]): Future[Assertion] =
      io.toTestFuture2.expectFailure(c)
  }

}
