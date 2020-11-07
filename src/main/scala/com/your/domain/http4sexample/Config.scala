package com.your.domain.http4sexample
import org.dhallj.syntax._
import org.dhallj.codec.syntax._
import us.oyanglul.dhall.generic._
import org.dhallj.imports.syntax._
import cats.effect._
import scala.concurrent.ExecutionContext
import org.http4s.client.blaze._
import org.http4s.Uri
import cats.effect.IO
import io.circe.generic.semiauto._

sealed trait Env

case object Local extends Env
case object PreProd extends Env
case object Prod extends Env

case class Application(env: Env, jokeService: Uri, database: DataBaseConfig)
case class Config(app: Application, sha: String)
case class DataBaseConfig(host: String, port: Int, name: String, user: String, pass: String) {
  def jdbc = s"jdbc:postgresql://${host}:${port}/${name}"
}
object DataBaseConfig {
  implicit val dbEncoder = deriveEncoder[DataBaseConfig]
}
object Config {
  def all(implicit cs: ContextShift[IO]) = {
    val client = BlazeClientBuilder[IO](ExecutionContext.global).resource
    client.use { implicit c =>
      IO.fromEither("classpath:/application.dhall".parseExpr)
        .flatMap(_.resolveImports[IO])
        .flatMap(expr => IO.fromEither(expr.normalize.as[Application]).map(Config(_, expr.normalize().hash())))
    }
  }
  implicit val dhallDecodeUri: org.dhallj.codec.Decoder[Uri] =
    org.dhallj.codec.Decoder.decodeString.map(Uri.unsafeFromString(_))
}

trait HasConfig {
  val config: Config
}
