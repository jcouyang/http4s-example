package com.your.domain

import cats.data._
import cats.effect.IO
import org.http4s.dsl._
import fs2._
import org.http4s.Request
import org.http4s.Response
import cats.arrow.FunctionK

package object http4sexample {
  def routeApp(
    pf: PartialFunction[Request[IO], App[Response[IO]]]
  ): Kleisli[OptionT[IO, *], Request[IO], App[Response[IO]]] =
    Kleisli { req =>
      OptionT(IO(pf.lift(req)))
    }
  type App[A] = Kleisli[IO, AppResource, A]
  type StreamApp[A] = Kleisli[Stream[IO, *], AppResource, A]
  object AppDsl extends Http4sDsl2[App, IO] {
    val liftG: FunctionK[IO, App] = NT.IOtoApp
  }
}
