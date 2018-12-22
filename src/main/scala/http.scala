package ziotest
package http

import scalaz.zio._
import ExitResult.Cause

// useful error type, you don't have to use it.
sealed trait ClientErrorBase
case class ClientError(message: String) extends ClientErrorBase
case class ClientBackendError(message: String) extends ClientErrorBase
case class ClientErrorAsThrowable(message:String) extends RuntimeException(message)

abstract sealed trait Message {
  def headers: Map[String, String]
  def body: String
}
case class HttpRequest(method: String, url: String, headers: Map[String, String], body: String)
    extends Message
case class HttpResponse(status: Int, headers: Map[String, String], body: String)
    extends Message

/** Simple client based on http4s. */
trait Client[E] {
  def fetch[A](request: HttpRequest)(handler: HttpResponse => IO[E, A]): IO[E,A]
  // expect status = 200, if not, apply orElse.
  // decoder applied only if 200 response received.
  // orElse applied only if non-200 response received.
  // other throws not caught and handled, the return type is lying
  def expectOr[A](request: HttpRequest, decoder: HttpResponse => IO[E,A])(
    orElse: (String, HttpResponse) => IO[E,A]): IO[E, A]

  // Like expectOr but explicitly handles throws, no lying in the API
  def expectOr[A](
    request: HttpRequest,
    decoder: HttpResponse => IO[E,A],
    handler: Throwable => Cause[E]
  )(
    orElse: (String, HttpResponse) => IO[E,A]
  ): IO[E,A]
}

object Client {
  // Run is the service to change a request to a response.
  // mkError allows us to customize error making for E
  def apply[E](
    run: HttpRequest => IO[E, HttpResponse]
      //,mkError: (String, Option[HttpResponse]) => IO[E,Nothing]
  ): Client[E] = new DefaultClient(run)
}

private [http] class DefaultClient[E](
  run: HttpRequest => IO[E, HttpResponse]
) extends Client[E] {
  import scala.util.Try

  def fetch[A](request: HttpRequest)(handler: HttpResponse => IO[E,A]): IO[E, A] =
    run(request).flatMap(handler)

  def expectOr[A](request: HttpRequest, decoder: HttpResponse => IO[E, A])(
    orElse: (String, HttpResponse) => IO[E, A]): IO[E, A] = {
    fetch(request){
      case goodResponse if goodResponse.status == 200 => decoder(goodResponse)
      case badResponse => orElse("bad status", badResponse)
    }
  }

  def expectOr[A](
    request: HttpRequest,
    decoder: HttpResponse => IO[E,A],
    // we make this explicit in this part of the API
    handler: Throwable => Cause[E]
  )(
    orElse: (String, HttpResponse) => IO[E,A]
  ): IO[E,A] = {
    // fromTry gives us: IO[Throwable, IO[E,O]]
    IO.fromTry[IO[E,A]](Try(expectOr[A](request, decoder)(orElse))).attempt.flatMap {
      case Right(a) => a
      case Left(e) => IO.fail0(handler(e))
    }
  }

}
  
