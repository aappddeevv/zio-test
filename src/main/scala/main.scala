package ziotest

import scalaz.zio._
import http._

object Main extends App {
  def run(args: List[String]): IO[Nothing, ExitStatus] = {
    IO.point(println("test1")).map(_ => ExitStatus.ExitNow(0))
  }
}
