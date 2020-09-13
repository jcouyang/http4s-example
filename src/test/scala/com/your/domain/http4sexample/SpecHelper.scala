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
    val config = Config.all.load[IO].unsafeRunSync()
    val jokeClient = mock[Client[IO]]
    val database = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      config.database.jdbc,
      config.database.user.value,
      config.database.pass.value,
      Blocker.liftExecutionContext(ExecutionContexts.synchronous),
    )
    val tracer = Trace.id
    val toggleMap = mock[ToggleMap]
  }
  implicit def router(implicit res: AppResource) = route.all.flatMapF(resp => OptionT.liftF(resp.run(res))).orNotFound

}
