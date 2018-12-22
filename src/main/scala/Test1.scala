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

  // all errors are "communication" backend errors.
  // using expectOr one layer up, is forced into backend error.
  val backendClient0 = Backend[BackendError](
    (s, respopt) => IO.fail(BackendError(s)),
    answers.get
  )

  // all low level errors are forced into ClientError.
  // expectOr errors are also forced into ClientError.
  val backendClient2 = Backend[ClientError](
    (s,respopt) => IO.fail(ClientError(s)),
    answers.get
  )

  // low level errors are ClientBackendError but
  // expectOr errors can be ClientError on per call basis
  val backendClient3 = Backend[ClientErrorBase](
    (s, respopt) => IO.fail(ClientBackendError(s)),
    answers.get
  )


}
