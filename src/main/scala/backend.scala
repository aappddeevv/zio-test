package ziotest
package backend

import scalaz.zio._
import http._

case class BackendError(message: String)
case class BackendErrorAsThrowable(message: String) extends RuntimeException(message)

/**
 * The Backend may know explicitly which errors it can throw, but it may lie in
 * that something below this layer could throw that the Backend is not aware
 * of. Or maybe the point is that by stating `E` we are declaring to the world
 * that we think we only generate `E` and anything else should be considered
 * world-ending/defect.
 */
object Backend {

  type Answers = HttpRequest => Option[HttpResponse]
  val message = "request not found in answers (comm failure)"

  // Supply error maker and responses to requests explicitly...totally faking it
  // :-). We could have used PartialFunction but wanted mkError to be a separate
  // function. mkError allows us to customize error making for E. mkError here
  // is really for "communications errors" where we do not get a response at
  // all!
  def apply[E](
    mkError: (String, Option[HttpResponse]) => IO[E,Nothing],
    answers: Answers
  ): Client[E] = {
    def service(request: HttpRequest): IO[E, HttpResponse] = {
      answers(request) match {
        case Some(resp) => IO.now(resp)
        case _ => mkError(message, None)
      }
    }
    Client(service)
  }
}
