package ziotest

import scalaz.zio._

object Main extends App {
  def run(args: List[String]): IO[Nothing, ExitStatus] = {
    IO.now(10).flatMap(_ => IO.now(ExitStatus.ExitNow(0):ExitStatus))
  }
}
