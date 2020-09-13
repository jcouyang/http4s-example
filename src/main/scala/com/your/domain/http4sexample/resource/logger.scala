package com.your.domain.http4sexample
package resource

import cats.Show
import cats.effect.IO
import cats.syntax.all._
import com.twitter.logging.Logger
import cats.data.Kleisli

object logger {
  implicit class LoggerSyntax(l: Logger) {
    def infoF[A: Show](text: A): App[Unit] =
      Kleisli { has =>
        IO(l.info(has.tracer.toString ++ text.show))
      }
  }
}
