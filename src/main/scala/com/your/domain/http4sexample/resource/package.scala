package com.your.domain.http4sexample

import cats.effect._

trait AppResource
    extends HasConfig
    with resource.HasClient
    with resource.HasDatabase
    with resource.HasToggle
    with resource.HasTracer
    with resource.HasLogger

package object resource {
  def mk(implicit ctx: ContextShift[IO]): Resource[IO, Resource[IO, AppResource]] =
    for {
      cfg <- Resource.liftF(Config.all)
      js <- http.mk(cfg.app.jokeService)
      db <- database.transactor(cfg.app.database)
    } yield Resource.make(IO {
      new AppResource {
        val config = cfg
        val jokeClient = js
        val database = db
      }
    }) { res =>
      res.logEval
    }
}
