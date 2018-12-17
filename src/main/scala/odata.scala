package ziotest
package odata

import scalaz.zio._
import ziotest.http._

case class ODataError(op: String, message: String, response: Option[HttpResponse])

trait ODataClient[E] {
  def create[A](entityType: String, body: String): IO[E, A]
  def delete(entityType: String, id: String): IO[E, Boolean]
}

/** Highest level of client. */
object ODataClient {

  def mkError[A](op: String)(message: String, response: HttpResponse): IO[ODataError,A] =
    IO.fail(ODataError(op, message, Option(response)))

  def apply[E](client: Client[E]) = new ODataClient[E] {
    def create[A](entityType: String, body: String): IO[E, A] =
      client.expectOr(HttpRequest("GET", s"/$entityType", Map(), """{ "Name":"Me"}"""),
        decoders.passthrough)(
        mkError)

    def delete(entityType: String, id: String): IO[E, Boolean] =
      client.expectOr(HttpRequest("DELETE", s"/$entityType($id)", Map(), ""),
        decoders.constant(true))(
        mkError)
  }
}

object decoders {
  def passthrough[E,String](response: HttpResponse): IO[E,String] = IO.now(response.body)
  def constant[E, A](constant: A)(response: HttpResponse): IO[E,A] = IO.now(constant)
}
