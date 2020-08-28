package com.your.domain.http4sexample
import cats._
import cats.data._
import cats.effect.IO

object NT {
  def AppToIO(implicit r:AppResource) = Lambda[App ~> IO]{_.run(r)}
  def IOtoApp = Lambda[IO ~> App](a=> Kleisli{_ => a})
  def OptionAppToOptionIO(implicit r: AppResource) = Lambda[OptionT[App,*] ~> OptionT[IO,*]]{a=>
    OptionT(a.value.run(r))
  }
}
