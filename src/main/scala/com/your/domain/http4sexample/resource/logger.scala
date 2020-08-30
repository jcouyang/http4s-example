package com.your.domain.http4sexample
package resource

import cats.Show
import cats.syntax.all._
import com.twitter.logging.Logger

trait HasLogger {
  def logInfo[A: Show](text: A)(implicit logger: Logger): App[Unit] =
    logger.info(text.show).pure[App]
}
