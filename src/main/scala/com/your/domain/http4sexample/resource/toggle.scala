package com.your.domain.http4sexample
package resource

import com.twitter.finagle.toggle.ToggleMap
import com.twitter.finagle.util.Rng

trait HasToggle {
  val toggleMap: ToggleMap
  def toggleOn(namespace: String): Boolean =
    toggleMap(namespace).isDefined && toggleMap(namespace).isEnabled(Rng.threadLocal.nextInt())
  def toggleOff(namespace: String): Boolean = !toggleOn(namespace)
}
