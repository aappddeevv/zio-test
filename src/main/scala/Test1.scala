package ziotest

import scalaz.zio._
import scalaz.zio.interop._
import odata._
import http._
import backend._

object Test1 {
  val good = HttpRequest("GET", "/good", Map(), "")
  val bad = HttpRequest("GET", "/bad", Map(), "") 
  val answers = Map(
    good -> HttpResponse(200, Map(), ""),
    bad -> HttpResponse(500, Map(), "")
  )
  val backendClient = Backend[BackendError](
    (s, respopt) => IO.fail(BackendError(s)),
    answers.get
  )
  // val odataClient = ODataClient[ODataError](
  //   backendClient,
  //   (s, resp) => IO.fail(ODataError("unknown", s, Option(resp)))<
  // )
}
