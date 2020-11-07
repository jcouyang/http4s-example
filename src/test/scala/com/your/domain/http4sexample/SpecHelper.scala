package com.your.domain.http4sexample

import cats.data.OptionT
import cats.effect.{Blocker, ContextShift, IO}
import com.twitter.finagle.toggle.ToggleMap
import com.twitter.finagle.tracing.Trace
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.http4s.client.Client
import org.mockito.MockitoSugar.mock
import org.http4s.implicits._
import scala.concurrent.ExecutionContext

trait SpecHelper {
  implicit val ctx: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  class TestAppResource extends AppResource {
    val config = Config.all.unsafeRunSync()
    val jokeClient = mock[Client[IO]]
    val database = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      config.app.database.jdbc,
      config.app.database.user,
      config.app.database.pass,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous),
    )
    override val tracer = Trace.id
    override val toggleMap = mock[ToggleMap]
  }
  implicit def router(implicit res: AppResource) = route.all.flatMapF(resp => OptionT.liftF(resp.run(res))).orNotFound

}
