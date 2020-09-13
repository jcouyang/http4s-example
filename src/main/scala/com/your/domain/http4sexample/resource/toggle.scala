package com.your.domain.http4sexample
package resource

import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.toggle.{StandardToggleMap, ToggleMap}
import com.twitter.finagle.util.Rng

trait HasToggle {
  val toggleMap: ToggleMap = StandardToggleMap("com.your.domain.http4sexample", DefaultStatsReceiver)
  def toggleOn(namespace: String): Boolean =
    toggleMap(namespace).isDefined && toggleMap(namespace).isEnabled(Rng.threadLocal.nextInt())
  def toggleOff(namespace: String): Boolean = !toggleOn(namespace)
}
