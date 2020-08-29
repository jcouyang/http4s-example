package com.your.domain.http4sexample

import scala.concurrent.ExecutionContext
import cats.effect._
import com.twitter.finagle.Http
import com.twitter.util.Await
import com.twitter.server.TwitterServer
import org.http4s.finagle.Finagle
import org.http4s.implicits._
import org.http4s._

object Main extends TwitterServer {
  implicit val ctx: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val port = flag("port", ":8080", "Service Port Number")
  def main() =
    resource.mk.use { implicit deps =>
      val service: HttpRoutes[IO] =
        route.all.mapF(resp => resp.flatMapF(_.run(deps).map(Some(_))))
      val server = Http.server
        .withLabel("http4s-example")
        .serve(port(), Finagle.mkService[IO](service.orNotFound))
      logger.info(s"Server Started on ${port()}")
      onExit {
        server.close()
        ()
      }
      IO(Await.ready(server))
    }.unsafeRunSync
}
