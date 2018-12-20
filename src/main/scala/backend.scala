package ziotest
package backend

import scalaz.zio._
import http._

case class BackendError(message: String)
case class BackendErrorAsThrowable(message: String) extends RuntimeException(message)

object Backend {

  type Answers = HttpRequest => Option[HttpResponse]

  // Supply error maker and responses to requests explicitly...totally faking it :-)
  // We could have used PartialFunction but wanted mkError to be a separate function.
  // mkError allows us to customize error making for E.
  def apply[E](
    mkError: (String, Option[HttpResponse]) => IO[E,Nothing],
    answers: Answers
  ): Client[E] = {
    def service(request: HttpRequest): IO[E, HttpResponse] = {
      answers(request) match {
        case Some(resp) => IO.now(resp)
        case _ => mkError("request not found in answer, cannot create response", None)
      }
    }
    Client(service, mkError)
  }
}
