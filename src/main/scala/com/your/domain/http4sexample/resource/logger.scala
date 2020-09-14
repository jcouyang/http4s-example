package com.your.domain.http4sexample
package resource

import cats.{Eval, Show}
import cats.effect.IO
import cats.syntax.all._
import com.twitter.logging.Logger
import cats.data.{Chain, Kleisli}
import cats.effect.concurrent._

trait HasLogger { self: HasTracer =>
  type Log = Eval[Unit]
  val loggerChannel: Ref[IO, Chain[Log]] = Ref.unsafe(Chain.empty[Log])
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
        has.loggerChannel.update(_.append(Eval.later(l.info(text.show))))
      }
  }
}
