import cats.effect._
import cats.syntax.all._
import org.flywaydb.core.Flyway
import scala.util.Properties._

object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val flyway = Flyway.configure.dataSource(
      s"jdbc:postgresql://${envOrElse("DB_HOST", "localhost")}:${envOrElse("DB_PORT", "5432")}/${envOrElse("DB_NAME", "joke")}",
      envOrElse("DB_USER", "postgres"),
      envOrElse("DB_PASS", ""),
    )
    args match {
      case List("migrate") =>
        IO(flyway.load.migrate()).as(ExitCode.Success)
      case List("clean") =>
        IO(flyway.load.clean()).as(ExitCode.Success)
      case a =>
        IO(System.err.println(s"""|Unknown args $a
             |Usage:
             |  sbt "db/run migrate|clean"                 migrate local
             |  env DB_HOST=<host> DB_PORT=<port> \\
             |      DB_NAME=<database> DB_USER=<user> \\
             |      DB_PASS=<pass> \\
             |      sbt "db/run migrate|clean"             migrate a specified db""".stripMargin)).as(ExitCode(2))
    }
  }
}
