package com.your.domain.http4sexample
package resource

import cats.effect._
import org.http4s._
import org.http4s.client.Client
import org.http4s.finagle.Finagle
import com.twitter.finagle.Http
import org.http4s.Uri.Scheme
import cats.data.Kleisli
import com.twitter.finagle.tracing.Trace
import zipkin2.finagle.http.HttpZipkinTracer

trait HasClient {
  val jokeClient: Client[IO]
  def jokeExpect[A: EntityDecoder[IO, *]](req: IO[Request[IO]]): App[A] =
    Kleisli
      .ask[IO, HasTracer]
      .flatMapF(
        a =>
          IO {
            a.tracer
          }.flatMap { id =>
            Trace.letId(id) {
              jokeClient.expect[A](req)
            }
          }
      )
}

object http {
  def mk(uri: Uri)(implicit ctx: ContextShift[IO]): Resource[IO, Client[IO]] =
    (uri.scheme, uri.host, uri.port) match {
      case (Some(Scheme.https), Some(host), None) =>
        Finagle.mkClient[IO](
          Http.client
            .withTls(host.value)
            .withTracer(new HttpZipkinTracer)
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
