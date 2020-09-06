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
import com.twitter.logging.Logger
import resource.logger._

object joke {
  implicit val log = Logger.get()
  case class DadJoke(id: String, joke: String)
  val dadJokeApp = log.infoF("getting dad joke...") *>
    Kleisli.ask[IO, HasClient].flatMapF(_.jokeClient.expect[DadJoke]("/"))

  val random = AppRoute {
    case GET -> Root / "random-joke" =>
      log.infoF("generating random joke") *>
      dadJokeApp.flatMap(Ok(_))
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
  val CRUD = AppRoute {
    case req @ POST -> Root / "joke" =>
      for {
        has <- Kleisli.ask[IO, HasDatabase]
        joke <- Kleisli.liftF(req.as[Repr.Create])
        id <- has.transact(run(quote {
          query[Dao.Joke]
            .insert(_.text -> lift(joke.text))
            .returningGenerated(_.id)
        }))
        _ <- log.infoF(s"created joke with id $id")
        resp <- Created(id)
      } yield resp

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

    case GET -> Root / "joke" / IntVar(id) =>
      log.infoF(s"getting joke $id") *>
      Kleisli
        .ask[IO, HasDatabase]
        .flatMap(_.transact(run(quote {
          query[Dao.Joke].filter(_.id == lift(id)).take(1)
        })))
        .flatMap {
          case a :: Nil => Ok(a)
          case _        =>
            log.infoF(s"cannot find joke $id") *>
            Kleisli.ask[IO, HasToggle].flatMap{ has=>
              if( has.toggleOn("com.your.domain.http4sexample.useDadJoke"))
                dadJokeApp.flatMap(NotFound(_))
              else
              NotFound(id)
            }
        }

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
