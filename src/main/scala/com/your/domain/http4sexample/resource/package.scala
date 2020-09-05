package com.your.domain.http4sexample

import cats.effect._
import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.toggle.StandardToggleMap
import com.twitter.finagle.tracing.Trace

trait AppResource
    extends HasConfig
    with resource.HasClient
    with resource.HasDatabase
    with resource.HasToggle
    with resource.HasTracer

package object resource {
  def mk(implicit ctx: ContextShift[IO]): Resource[IO, Resource[IO, AppResource]] =
    for {
      cfg <- Resource.liftF(Config.all.load[IO])
      js <- http.mk(cfg.jokeService)
      db <- database.transactor
    } yield Resource.liftF(IO {
      new AppResource {
        val config = cfg
        val jokeClient = js
        val database = db
        val toggleMap = StandardToggleMap("com.your.domain.http4sexample", DefaultStatsReceiver)
        val tracer = Trace.id
      }
    })
}
