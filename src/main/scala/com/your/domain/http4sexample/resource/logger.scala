package com.your.domain.http4sexample
package resource

import cats.{Eval, Functor, Show}
import cats.effect.IO
import cats.syntax.all._
import com.twitter.logging.Logger
import cats.data.{Chain, Kleisli}
import cats.effect.concurrent._
import cats.mtl.{Tell}

trait HasLogger { self: HasTracer =>
  type Log = Eval[Unit]
  val loggerChannel: Ref[IO, Chain[Log]] = Ref.unsafe(Chain.empty[Eval[Unit]])
  implicit val loggerTeller: Tell[IO, Log] = new Tell[IO, Log] {
    def functor: Functor[IO] = Functor[IO]
    def tell(l: Log): IO[Unit] = loggerChannel.update(_.append(l))
  }
  def logEval =
    loggerChannel.get.map { l =>
      org.slf4j.MDC.put("trace.id", tracer.toString)
      l.sequence_.value
    }
}
object logger {
  implicit class LoggerSyntax(l: Logger) {
    def infoF[A: Show](text: A): Kleisli[IO, AppResource, Unit] =
      Kleisli { has =>
        has.loggerTeller.tell(Eval.later(l.info(text.show)))
      }
  }
}
