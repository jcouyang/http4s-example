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
import org.http4s.HttpRoutes
import org.http4s.HttpApp

class JokeSpec extends FunSuite with ScalaCheckSuite {
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
  def update(id: String, req: joke.Repr.Create)(implicit router: HttpApp[IO]) =
    router(PUT(req.asJson, uri("http://localhost/joke") / id).unsafeRunSync())

  def query(id: String)(implicit router: HttpApp[IO]) = router(GET(uri("http://localhost/joke") / id).unsafeRunSync())
  private def create(req: joke.Repr.Create)(implicit router: HttpApp[IO]) =
    for {
      reqCreate <- POST(req.asJson, uri("http://localhost/joke"))
      created <- router(reqCreate)
      id <- created.as[Json].map(_.hcursor.get[Int]("id"))
    } yield id.getOrElse(-1).toString

  private def delete(id: String)(implicit router: HttpApp[IO]) =
    router(DELETE(uri("http://localhost/joke") / id).unsafeRunSync())
      .map(_ => assertEquals(query(id).unsafeRunSync().status, NotFound))

  def createAndDelete(req: joke.Repr.Create)(implicit router: HttpApp[IO]) =
    Resource.make[IO, String](create(req))(delete)

  property("CRUD") {
    implicit val appRes = new TestAppResource
    forAll { (requestBody: joke.Repr.Create, updateBody: joke.Repr.Create) =>
      when(appRes.toggleMap.apply("com.your.domain.http4sexample.useDadJoke"))
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

  test("get dad joke when toggle on") {
    val dadJoke = json"""{
        "id": "R7UfaahVfFd",
        "joke": "My dog used to chase people on a bike a lot. It got so bad I had to take his bike away."
      }"""
    implicit val appRes = new TestAppResource {
      override val jokeClient = Client.fromHttpApp(
        HttpRoutes
          .of[IO] {
            case GET -> _ => Ok(dadJoke)
          }
          .orNotFound
      )
    }

    when(appRes.toggleMap.apply("com.your.domain.http4sexample.useDadJoke"))
      .thenReturn(Toggle.on("com.your.domain.http4sexample.useDadJoke"))

    val resp1 = router.run(GET(uri("http://localhost/joke/123")).unsafeRunSync()).flatMap(_.as[Json])
    assertEquals(resp1.unsafeRunSync(), dadJoke)

    when(appRes.toggleMap.apply("com.your.domain.http4sexample.useDadJoke"))
      .thenReturn(Toggle.off("com.your.domain.http4sexample.useDadJoke"))

    val resp2 = router.run(GET(uri("http://localhost/joke/123")).unsafeRunSync())
    assertEquals(resp2.unsafeRunSync().status, NotFound)
  }
}
