package com.your.domain.http4sexample
package resource

import cats.effect._
import doobie._
import doobie.hikari._
import io.getquill.{idiom => _, _}
import doobie.quill.DoobieContext
import doobie.implicits._
import fs2._

trait HasDatabase {
  val database: Transactor[IO]
  def transact[A](c: ConnectionIO[A]): App[A] =
    NT.IOtoApp(c.transact(database))

  def transact[A](c: Stream[ConnectionIO, A]): Stream[IO, A] =
    c.transact(database)
}

object database {
  val context = new DoobieContext.Postgres(SnakeCase)
  def transactor(implicit ctx: ContextShift[IO]): Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      be <- Blocker[IO]
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        "jdbc:postgresql:joke",
        "postgres",
        "",
        ce,
        be
      )
    } yield xa
}
