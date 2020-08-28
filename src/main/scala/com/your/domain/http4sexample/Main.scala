package com.your.domain.http4sexample

import scala.concurrent.ExecutionContext
import cats.effect._
import com.twitter.finagle.Http
import com.twitter.util.Await
import com.twitter.server.TwitterServer
import org.http4s.finagle.Finagle
import org.http4s.implicits._
import org.http4s._
import cats.data._

object Main extends TwitterServer {
  implicit val ctx: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val port = flag("port", ":8080", "Service Port Number")
  val resource = AppResource.apply
  def main() = {
    resource.use { implicit deps =>
      val service: HttpRoutes[IO] =
        route.all
          .mapK[OptionT[IO, *]](NT.OptionAppToOptionIO)
          .map(_.mapK(NT.AppToIO))
          .local(_.mapK(NT.IOtoApp))
      val server = Http
        .server
        .withLabel("http4s-example")
        .serve(port(), Finagle.mkService[IO](service.orNotFound))
      logger.info(s"Server Started on ${port()}")
      onExit { server.close()
      ()}
      IO(Await.ready(server))
    }
  }.unsafeRunSync
}
