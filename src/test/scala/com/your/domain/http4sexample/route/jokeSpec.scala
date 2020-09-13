package com.your.domain.http4sexample
package route
import munit._
import org.scalacheck.Prop._
import org.http4s.Uri._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec._
import org.mockito.MockitoSugar._
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalacheck.ScalacheckShapeless._
import io.circe.literal._
import io.circe._
import cats.effect._
import scala.concurrent.ExecutionContext
import org.http4s.client.Client
import com.twitter.finagle.tracing.Trace
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import cats.data.OptionT
import com.twitter.finagle.toggle._

class JokeSpec extends FunSuite with ScalaCheckSuite {
  implicit val ctx: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  val mockToggleMap = mock[ToggleMap]
  val testAppResource =
    for {
      cfg <- Resource.liftF(Config.all.load[IO])
    } yield (new AppResource {
      val config = cfg
      val jokeClient = mock[Client[IO]]
      val database = Transactor.fromDriverManager[IO](
        "org.postgresql.Driver",
        cfg.database.jdbc,
        cfg.database.user.value,
        cfg.database.pass.value,
        Blocker.liftExecutionContext(ExecutionContexts.synchronous),
      )
      val tracer = Trace.id
      val toggleMap = mockToggleMap

    })

  val router = route.all.flatMapF(resp => OptionT.liftF(testAppResource.use(r => resp.run(r)))).orNotFound
  def update(id: String, req: joke.Repr.Create) =
    router(PUT(req.asJson, uri("http://localhost/joke") / id).unsafeRunSync())

  def query(id: String) = router(GET(uri("http://localhost/joke") / id).unsafeRunSync())
  private def create(req: joke.Repr.Create) =
    for {
      reqCreate <- POST(req.asJson, uri("http://localhost/joke"))
      created <- router(reqCreate)
      id <- created.as[Json].map(_.hcursor.get[Int]("id"))
    } yield id.getOrElse(-1).toString

  private def delete(id: String) =
    router(DELETE(uri("http://localhost/joke") / id).unsafeRunSync())
      .map(_ => assertEquals(query(id).unsafeRunSync().status, NotFound))

  def createAndDelete(req: joke.Repr.Create) = Resource.make[IO, String](create(req))(delete)

  property("CRUD") {
    forAll { (requestBody: joke.Repr.Create, updateBody: joke.Repr.Create) =>
      when(mockToggleMap.apply("com.your.domain.http4sexample.useDadJoke"))
        .thenReturn(Toggle.off("com.your.domain.http4sexample.useDadJoke"))
      createAndDelete(requestBody)
        .use { id =>
          assertEquals(query(id).flatMap(_.as[joke.Repr.View]).unsafeRunSync().text, requestBody.text)
          update(id, updateBody)
            .map(_ => assertEquals(query(id).flatMap(_.as[joke.Repr.View]).unsafeRunSync().text, updateBody.text))
        }
        .unsafeRunSync()
    }
  }
}
