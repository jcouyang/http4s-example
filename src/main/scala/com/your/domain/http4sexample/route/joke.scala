package com.your.domain.http4sexample
package route

import resource._
import database.context._
import AppDsl._
import cats.data._
import cats.effect._
import cats.implicits._
import org.http4s.circe.CirceEntityCodec._
import io.circe.generic.auto._
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId
import doobie.implicits._

object Joke {
  case class Joke(joke: String)
  val random = AppRoute {
    case GET -> Root / "random-joke" =>
      for {
        jokeClient <- Kleisli.ask[IO, HasClient].map(_.jokeClient)
        config <- Kleisli.ask[IO, HasConfig].map(_.config)
        joke <- Kleisli.liftF(jokeClient.expect[Joke](config.jokeService))
        resp <- Ok(joke)
      } yield resp
  }
  object Dao {
    case class Joke(id: Int, text: String, created: Instant)
  }
  object Repr {
    case class View(id: Int, text: String, created: ZonedDateTime)
    case class Create(text: String)
    object View {
      def from(db: Dao.Joke) = View(db.id, db.text, ZonedDateTime.ofInstant(db.created, ZoneId.systemDefault()))
    }
  }
  implicit val InstantDecoder: Decoder[Instant] =
    decoder((index, row) => row.getTimestamp(index).toInstant())

  val CURD = AppRoute {
    case GET -> Root / "joke" =>
      Kleisli
        .ask[IO, HasDatabase]
        .flatMap(
          db =>
            Ok(
              db.transact(stream(quote {
                query[Dao.Joke]
              }))
                .map(Repr.View.from)
            )
        )

    case req @ POST -> Root / "joke" =>
      for {
        db <- Kleisli.ask[IO, HasDatabase]
        joke <- Kleisli.liftF(req.as[Repr.Create])
        id <- db.transact(run(quote {
          query[Dao.Joke].insert(_.text -> lift(joke.text))
        }) >> sql"select lastval()".query[Int].unique)
        resp <- Created(id)
      } yield resp

    case req @ PUT -> Root / "joke" / IntVar(id) =>
      for {
        db <- Kleisli.ask[IO, HasDatabase]
        joke <- Kleisli.liftF(req.as[Repr.Create])
        _ <- db.transact(run(quote {
          query[Dao.Joke].update(_.id -> lift(id), _.text -> lift(joke.text))
        }))
        resp <- NoContent()
      } yield resp

    case DELETE -> Root / "joke" / IntVar(id) =>
      Kleisli
        .ask[IO, HasDatabase]
        .flatMap(_.transact(run(quote {
          query[Dao.Joke].filter(_.id == lift(id)).delete
        }))) >> NoContent()
  }
}
