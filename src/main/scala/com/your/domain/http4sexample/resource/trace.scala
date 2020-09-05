package com.your.domain.http4sexample
package resource

import com.twitter.finagle.tracing.TraceId

trait HasTracer {
  val tracer: TraceId
}
