package db
import cats.effect._
import doobie.free.connection.ConnectionIO
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor

  trait DoobieMigration extends BaseJavaMigration {
    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
    def migrate: ConnectionIO[_]
    override def migrate(context: Context): Unit = {
        val yolo = Transactor.fromConnection[IO](context.getConnection, Blocker.liftExecutionContext(ExecutionContexts.synchronous)).yolo
        import yolo._
        migrate.quick.unsafeRunSync()
      }
    }