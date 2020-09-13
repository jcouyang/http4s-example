package com.your.domain.http4sexample
import ciris._
import org.http4s.Uri
import enumeratum._
import io.circe.Encoder
import org.http4s.EntityEncoder
import org.http4s.circe._
import cats.effect.IO
import cats.implicits._
import io.circe.generic.semiauto._
import cats.Show

sealed trait Env extends EnumEntry

object Env extends Enum[Env] with CirisEnum[Env] {
  case object Local extends Env
  case object PreProd extends Env
  case object Prod extends Env

  val values = findValues
  implicit val envEncoder: Encoder[Env] = Encoder.encodeString.contramap(_.toString())
}
import Env._

case class Config(env: Env, jokeService: Uri, database: DataBaseConfig)
case class DataBaseConfig(host: Secret[String], port: Int, name: String, user: Secret[String], pass: Secret[String]) {
  def jdbc = s"jdbc:postgresql://${host.value}:${port}/${name}"
}
object DataBaseConfig {
  implicit def secretEncoder[A: Show]: Encoder[Secret[A]] = Encoder.encodeString.contramap(_.show)
  implicit val dbEncoder = deriveEncoder[DataBaseConfig]
}
object Config {
  val appEnv = env("APP_ENV").as[Env]

  def all =
    (appEnv, database).mapN { (env, database) =>
      Config(env = env, jokeService = Uri.uri("https://icanhazdadjoke.com"), database = database)
    }

  def database =
    (
      env("DB_HOST").default("localhost").secret,
      env("DB_PORT").as[Int].default(5432),
      env("DB_NAME").default("joke"),
      env("DB_USER").default("postgres").secret,
      env("DB_PASS").default("").secret,
    ).mapN(DataBaseConfig.apply)
  implicit val configEncoder: Encoder[Config] = deriveEncoder[Config]
  implicit val configEntityEncoder: EntityEncoder[IO, Config] = jsonEncoderOf[IO, Config]
}

trait HasConfig {
  val config: Config
}
