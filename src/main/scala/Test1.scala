package ziotest

import scalaz.zio._
import scalaz.zio.interop._
import odata._
import http._
import backend._

object Test1 {
  val good = HttpRequest("GET", "/good", Map(), "")
  val bad = HttpRequest("GET", "/bad", Map(), "")
  val notInAnswers = HttpRequest("GET", "/notinanswers", Map(), "")
  val answers = Map(
    good -> HttpResponse(200, Map(), ""),
    bad -> HttpResponse(500, Map(), "")
  )

  // all errors are "communication" backend errors
  // using expectOr one layer up, is forced into backend error
  val backendClient = Backend[BackendError](
    (s, respopt) => IO.fail(BackendError(s)),
    answers.get
  )

  // all errors are forced into ClientErrors, including
  // errors from the lower level.
  val backendClient2 = Backend[ClientError](
    (s,respopt) => IO.fail(ClientError(s, None)),
    answers.get
  )
}
