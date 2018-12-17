package ziotest
package http

import scalaz.zio._

// useful error type, you don't have to use it.
class ClientError(message: String, cause: Option[Throwable])

class Message(headers: Map[String, String], body: String)
case class HttpRequest(method: String, url: String, headers: Map[String, String], body: String)
    extends Message(headers, body)
case class HttpResponse(status: Int, headers: Map[String, String], body: String)
    extends Message(headers, body)

/** Simple client based on http4s. */
trait Client[E] {
  def fetch[A](request: HttpRequest)(handler: HttpResponse => IO[E, A]): IO[E, A]
  // expect status = 200, if not, apply orElse
  def expectOr[A](request: HttpRequest, decoder: HttpResponse => IO[E,A])(
    orElse: (String, HttpResponse) => IO[E,A]): IO[E, A]
}

object Client {
  def apply[E](run: HttpRequest => IO[E, HttpResponse],
    mkError: (String, Option[HttpResponse]) => IO[E,Nothing]): Client[E] =
    new DefaultClient(run, mkError)
}

private [http] class DefaultClient[E](run: HttpRequest => IO[E, HttpResponse],
  mkError: (String, Option[HttpResponse]) => IO[E,Nothing])
    extends Client[E] {
  def fetch[A](request: HttpRequest)(handler: HttpResponse => IO[E,A]): IO[E, A] =
    run(request).flatMap(handler)

  def expectOr[A](request: HttpRequest, decoder: HttpResponse => IO[E, A])(
    orElse: (String, HttpResponse) => IO[E, A]): IO[E, A] = {
    fetch(request){
      case goodResponse if goodResponse.status == 200 => decoder(goodResponse)
      case badResponse => mkError("received bad response", Option(badResponse))
    }
  }

}
  
