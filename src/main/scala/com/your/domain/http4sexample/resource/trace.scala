package com.your.domain.http4sexample
package resource

import com.twitter.finagle.tracing.{Trace, TraceId}

trait HasTracer {
  val tracer: TraceId = Trace.id
}
