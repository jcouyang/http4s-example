package com.your.domain.http4sexample

import cats.syntax.all._

package object route {
  val all = joke.CRUD <+> joke.random <+> config.get
}
