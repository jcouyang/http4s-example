package com.your.domain.http4sexample
package route

import org.http4s._
import AppDsl._
import cats.data._
import cats.effect._
import org.http4s.circe.CirceEntityCodec._
import io.circe.generic.auto._

object Joke {
  case class Joke(joke: String)
  val get = HttpRoutes.of[App] {
    case GET -> Root / "joke" => for {
      jokeClient <- Kleisli.ask[IO, HasClient].map(_.jokeClient)
      config <- Kleisli.ask[IO, HasConfig].map(_.config)
      joke <- Kleisli.liftF(jokeClient.expect[Joke](config.jokeService))
      resp <- Ok(joke)
    } yield resp
  }
}
