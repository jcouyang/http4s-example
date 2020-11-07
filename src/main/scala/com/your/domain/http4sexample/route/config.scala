package com.your.domain.http4sexample
package route

import AppDsl._
import cats.data._
import cats.effect._
import cats.implicits._
import com.twitter.logging.Logger
import resource.logger._

object config {
  implicit val log = Logger.get()
  val get = AppRoute {
    case GET -> Root / "diagnostic" / "config" =>
      log.infoF("getting config sha") *>
        Kleisli.ask[IO, HasConfig].flatMap(h => Ok(h.config.sha))
  }
}
