package ziotest
package odata

import scalaz.zio._
import ziotest.http._

case class ODataError(op: String, message: String, response: Option[HttpResponse])
case class ODataErrorAsThrowable(message: String) extends RuntimeException(message)

trait ODataClient[E] {
  // return the new object content as a string, like a JSON string
  def create[A](entityType: String, body: String): IO[E, String]
  // true => entity instance was deleted
  def delete(entityType: String, id: String, outcome: Boolean): IO[E, Boolean]
}

/** Highest level of client. */
object ODataClient {

  def mkError[A](op: String)(message: String, response: HttpResponse): IO[ODataError,A] =
    IO.fail(ODataError(op, message, Option(response)))

  // Client changes request => response.
  // mkError allows us to customize the expectOr errors, not communication errors.
  def apply[E](
    client: Client[E],
    mkError: (String, HttpResponse) => IO[E, Nothing]
  ) = new ODataClient[E] {
    // just pass through the body as the "created" JSON payload...super cheaty 
    def create[A](entityType: String, body: String): IO[E, String] =
      client.expectOr(HttpRequest("GET", s"/$entityType", Map(), """{ "Name":"Me"}"""),
        decoders.passthrough)(
        mkError)

    // outcome allows us to pre-determine delete's success
    def delete(entityType: String, id: String, outcome: Boolean = true): IO[E, Boolean] =
      client.expectOr(HttpRequest("DELETE", s"/$entityType($id)", Map(), ""),
        decoders.constant(outcome))(
        mkError)
  }
}

object decoders {
  def passthrough[E](m: Message): IO[E,String] = IO.now(m.body)
  def constant[E, A](constant: A)(response: HttpResponse): IO[E,A] = IO.now(constant)
}
