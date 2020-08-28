package com.your.domain.http4sexample

import resource._
import cats.effect._
import org.http4s._
import org.http4s.client.Client
import org.http4s.finagle.Finagle
import com.twitter.finagle.Http
import org.http4s.Uri.Scheme

trait HasClient {
  val jokeClient: Client[IO]
}

trait AppResource extends HasConfig with HasClient with HasDatabase

object AppResource {
  def apply(implicit ctx: ContextShift[IO]): Resource[IO, AppResource] =
    for {
      cfg <- Resource.liftF(Config.all.load[IO])
      js <- mkHttp4sClient(cfg.jokeService)
      db <- database.transactor
    } yield new AppResource {
      val config = cfg
      val jokeClient = js
      val database = db
    }
  private def mkHttp4sClient(uri: Uri)(implicit ctx: ContextShift[IO]): Resource[IO, Client[IO]] =
    (uri.scheme, uri.host, uri.port) match {
      case (Some(Scheme.https), Some(host), None) =>
        Finagle.mkClient[IO](
          Http.client
            .withTls(host.value)
            .withHttpStats
            .newService(s"$host:443")
        )
      case (Some(Scheme.https), Some(host), Some(port)) =>
        Finagle.mkClient[IO](
          Http.client
            .withTls(host.value)
            .withHttpStats
            .newService(s"$host:$port")
        )
      case (_, Some(host), Some(port)) =>
        Finagle.mkClient[IO](
          Http.client.withHttpStats
            .newService(s"$host:$port")
        )
      case _ =>
        Resource.liftF(IO.raiseError(new Exception(s"cannot initialize HttpClient for $uri")))
    }
}
