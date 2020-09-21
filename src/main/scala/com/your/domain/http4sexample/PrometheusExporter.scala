package com.your.domain.http4sexample

import com.twitter.server.TwitterServer
import com.twitter.server.AdminHttpServer.Route
import com.twitter.server.Admin.Grouping
import com.samstarling.prometheusfinagle.metrics.{MetricsService}
import io.prometheus.client.CollectorRegistry
import com.samstarling.prometheusfinagle.PrometheusStatsReceiver

trait PrometheusExporter { self: TwitterServer =>
  val metricsRoute: Route = Route.isolate(
    Route(
      path = "/metrics",
      handler = new MetricsService(PrometheusExporter.metricRegistry),
      alias = "Prometheus Metrics",
      group = Some(Grouping.Metrics),
      includeInIndex = true,
    )
  )
  addAdminRoute(metricsRoute)
}

object PrometheusExporter {
  val metricRegistry = CollectorRegistry.defaultRegistry
  val metricStatsReceiver = new PrometheusStatsReceiver(metricRegistry)
}
