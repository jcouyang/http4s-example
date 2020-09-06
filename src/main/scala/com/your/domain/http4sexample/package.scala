package com.your.domain

import cats.data._
import cats.effect.IO
import org.http4s.dsl._
import org.http4s.Request
import org.http4s.Response
import cats.arrow.FunctionK

package object http4sexample {
  type App[A] = Kleisli[IO, AppResource, A]
  type AppRoute = Kleisli[OptionT[IO, *], Request[IO], App[Response[IO]]]
  def AppRoute(pf: PartialFunction[Request[IO], App[Response[IO]]]): AppRoute =
    Kleisli { req =>
      OptionT(IO(pf.lift(req)))
    }

  object AppDsl extends Http4sDsl2[App, IO] {
    val liftG: FunctionK[IO, App] = NT.IOtoApp
  }
}
