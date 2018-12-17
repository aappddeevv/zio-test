package ziotest
package backend

import scalaz.zio._
import http._

case class BackendError(message: String)

object Backend {
  // Supply error maker and responses to requesets explicitly.
  def apply[E](mkError: (String, Option[HttpResponse]) => IO[E,Nothing],
    answers: Map[HttpRequest, HttpResponse]): Client[E] = {
    def service(request: HttpRequest): IO[E, HttpResponse] = {
      answers.get(request) match {
        case Some(resp) => IO.now(resp)
        case _ => mkError("request not found in answer, cannot create response", None)
      }
    }
    Client(service, mkError)
  }
}
