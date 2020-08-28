package com.your.domain

import io.circe.Encoder
import cats.data._
import cats.effect.IO
import org.http4s.dsl._
import org.http4s.circe._
import org.http4s.EntityEncoder

package object http4sexample {
  object AppDsl extends Http4sDsl[App]
  type App[A] = Kleisli[IO, AppResource, A]

  implicit def circeEntityEncoder[A: Encoder]: EntityEncoder[App, A] =
    jsonEncoderOf[App, A]
  // implicit def appEntityEncoder[A](encoder: EntityEncoder[IO, A]): EntityEncoder[App, A] = {
  //   new EntityEncoder[App, A] {
  //     override def toEntity(a: A): Entity[App] = {
  //       val entity = encoder.toEntity(a)
  //       new Entity{
  //         val body = entity.body.co
  //         val length = entity.length
  //     }
  //     }
  //     override def headers: Headers = encoder.headers
  //   }
  // }
}
