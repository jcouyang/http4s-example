package com.your.domain.http4sexample
package route
import munit._
import org.scalacheck.Prop._
// import cats.effect.IO
import org.http4s.Uri._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityCodec._
import doobie._
import org.mockito.MockitoSugar._
import org.mockito.ArgumentMatchersSugar._
import cats.data.Kleisli
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalacheck.ScalacheckShapeless._
// import org.scalacheck.Arbitrary.arbitrary
import io.circe.literal._
import io.circe._

class JokeSpec extends FunSuite with ScalaCheckSuite {
  // val b = run(quote{query[Dao.Joke]})
  val appResource = mock[AppResource]
  val router = route.all.mapF(resp => resp.flatMapF(_.run(appResource).map(Some(_)))).orNotFound
  property("Create") {
    forAll { (requestBody: joke.Repr.Create) =>
      when(appResource.transact(any[ConnectionIO[Int]]))
        .thenReturn(Kleisli.pure(1))
      val req = POST(requestBody.asJson, uri("http://localhost/joke")).unsafeRunSync()
      val resp = router(req).unsafeRunSync()

      assertEquals(resp.as[Json].unsafeRunSync(), json"1")
    }
  }

  // test("Delete") {
  //   val obtained = 42
  //   val expected = 43
  //   assertEquals(obtained, expected)
  // }
}
