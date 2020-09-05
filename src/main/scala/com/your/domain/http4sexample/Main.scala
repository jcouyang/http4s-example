package com.your.domain.http4sexample

import scala.concurrent.ExecutionContext
import cats.effect._
import com.twitter.finagle.Http
import com.twitter.util.Await
import com.twitter.server.TwitterServer
import org.http4s.finagle.Finagle
import org.http4s.implicits._
import zipkin2.finagle.http.HttpZipkinTracer
import org.http4s.HttpRoutes

object Main extends TwitterServer {
  implicit val ctx: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val port = flag("port", ":8080", "Service Port Number")
  val options = cats.effect.tracing.PrintingOptions.Default
    .withShowFullStackTraces(true)
    .withMaxStackTraceLines(8)
  def main() =
    resource.mk
      .use { (deps: Resource[IO, AppResource]) =>
        val service: HttpRoutes[IO] = route.all.mapF(
          resp =>
            resp.flatMapF(
              app =>
                deps.use { r =>
                  for {
                    res <- app.run(r)
                    trace <- IO.trace
                    _ <- trace.printFiberTrace(options)
                  } yield Some(res)

                }
            )
        )
        val server = Http.server
          .withTracer(new HttpZipkinTracer)
          .withLabel("http4s-example")
          .serve(port(), Finagle.mkService[IO](service.orNotFound))
        logger.info(s"Server Started on ${port()}")
        onExit {
          server.close()
          ()
        }
        IO(Await.ready(server))
      }
      .unsafeRunSync()
}
