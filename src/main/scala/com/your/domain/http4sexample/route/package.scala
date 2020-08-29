package com.your.domain.http4sexample

import cats.syntax.all._

package object route {
  val all = Joke.CURD <+> Joke.random
}
