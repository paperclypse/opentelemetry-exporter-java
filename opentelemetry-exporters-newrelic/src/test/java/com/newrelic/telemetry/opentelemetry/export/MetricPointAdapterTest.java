/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.opentelemetry.export;

import static com.newrelic.telemetry.opentelemetry.export.AttributeNames.SERVICE_NAME;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.Summary;
import io.opentelemetry.common.Labels;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class MetricPointAdapterTest {

  @Test
  void testLongPoint() {
    TimeTracker timeTracker = mock(TimeTracker.class);
    when(timeTracker.getPreviousTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(9_000L));
    MetricPointAdapter metricPointAdapter = new MetricPointAdapter(timeTracker);

    Attributes commonAttributes = new Attributes().put(SERVICE_NAME, "fooService");
    Descriptor metricDescriptor =
        Descriptor.create(
            "metricName",
            "metricDescription",
            "units",
            Type.MONOTONIC_LONG,
            Labels.of("commonKey", "commonValue"));

    LongPoint longPoint =
        LongPoint.create(
            100,
            TimeUnit.MILLISECONDS.toNanos(10_000L),
            Labels.of("specificKey", "specificValue"),
            123L);

    Attributes expectedAttributes =
        new Attributes().put(SERVICE_NAME, "fooService").put("specificKey", "specificValue");
    Count expectedMetric = new Count("metricName", 123L, 9_000L, 10_000L, expectedAttributes);

    Collection<Metric> result =
        metricPointAdapter.buildMetricsFromPoint(
            metricDescriptor, Type.MONOTONIC_LONG, commonAttributes, longPoint);

    assertEquals(singleton(expectedMetric), result);
  }

  @Test
  void testLongPoint_nonMonotonic() {
    TimeTracker timeTracker = mock(TimeTracker.class);
    MetricPointAdapter metricPointAdapter = new MetricPointAdapter(timeTracker);

    Attributes commonAttributes = new Attributes().put(SERVICE_NAME, "fooService");
    Descriptor metricDescriptor =
        Descriptor.create(
            "metricName",
            "metricDescription",
            "units",
            Type.NON_MONOTONIC_LONG,
            Labels.of("commonKey", "commonValue"));

    LongPoint longPoint =
        LongPoint.create(
            100,
            TimeUnit.MILLISECONDS.toNanos(10_000L),
            Labels.of("specificKey", "specificValue"),
            123L);

    Attributes expectedAttributes =
        new Attributes().put(SERVICE_NAME, "fooService").put("specificKey", "specificValue");
    Gauge expectedMetric = new Gauge("metricName", 123L, 10_000L, expectedAttributes);

    Collection<Metric> result =
        metricPointAdapter.buildMetricsFromPoint(
            metricDescriptor, Type.NON_MONOTONIC_LONG, commonAttributes, longPoint);

    assertEquals(singleton(expectedMetric), result);
  }

  @Test
  void testDoublePoint() {
    TimeTracker timeTracker = mock(TimeTracker.class);
    when(timeTracker.getPreviousTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(9_000L));
    MetricPointAdapter metricPointAdapter = new MetricPointAdapter(timeTracker);

    Attributes commonAttributes = new Attributes().put(SERVICE_NAME, "fooService");
    DoublePoint doublePoint =
        DoublePoint.create(
            100,
            TimeUnit.MILLISECONDS.toNanos(10_000L),
            Labels.of("specificKey", "specificValue"),
            123.55d);
    Descriptor metricDescriptor =
        Descriptor.create(
            "metricName",
            "metricDescription",
            "units",
            Type.MONOTONIC_DOUBLE,
            Labels.of("commonKey", "commonValue"));

    Attributes expectedAttributes =
        new Attributes().put(SERVICE_NAME, "fooService").put("specificKey", "specificValue");
    Count expectedMetric = new Count("metricName", 123.55d, 9_000L, 10_000L, expectedAttributes);

    Collection<Metric> result =
        metricPointAdapter.buildMetricsFromPoint(
            metricDescriptor, Type.MONOTONIC_DOUBLE, commonAttributes, doublePoint);

    assertEquals(singleton(expectedMetric), result);
  }

  @Test
  void testDoublePoint_nonMonotonic() {
    TimeTracker timeTracker = mock(TimeTracker.class);
    MetricPointAdapter metricPointAdapter = new MetricPointAdapter(timeTracker);

    Attributes commonAttributes = new Attributes().put(SERVICE_NAME, "fooService");
    Descriptor metricDescriptor =
        Descriptor.create(
            "metricName",
            "metricDescription",
            "units",
            Type.NON_MONOTONIC_DOUBLE,
            Labels.of("commonKey", "commonValue"));
    DoublePoint doublePoint =
        DoublePoint.create(
            100,
            TimeUnit.MILLISECONDS.toNanos(10_000L),
            Labels.of("specificKey", "specificValue"),
            123.55d);

    Attributes expectedAttributes =
        new Attributes().put(SERVICE_NAME, "fooService").put("specificKey", "specificValue");
    Gauge expectedMetric = new Gauge("metricName", 123.55d, 10_000L, expectedAttributes);

    Collection<Metric> result =
        metricPointAdapter.buildMetricsFromPoint(
            metricDescriptor, Type.NON_MONOTONIC_DOUBLE, commonAttributes, doublePoint);

    assertEquals(singleton(expectedMetric), result);
  }

  @Test
  void testSummaryPoint() {
    TimeTracker timeTracker = mock(TimeTracker.class);
    when(timeTracker.getPreviousTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(9_000L));
    MetricPointAdapter metricPointAdapter = new MetricPointAdapter(timeTracker);

    Attributes commonAttributes = new Attributes().put(SERVICE_NAME, "fooService");
    ValueAtPercentile min = ValueAtPercentile.create(0.0, 5.5d);
    ValueAtPercentile max = ValueAtPercentile.create(100.0, 100.01d);
    Descriptor metricDescriptor =
        Descriptor.create(
            "metricName",
            "metricDescription",
            "units",
            Type.SUMMARY,
            Labels.of("commonKey", "commonValue"));

    SummaryPoint summary =
        SummaryPoint.create(
            TimeUnit.MILLISECONDS.toNanos(9_000L),
            TimeUnit.MILLISECONDS.toNanos(10_000L),
            Labels.of("specificKey", "specificValue"),
            200,
            123.55d,
            Arrays.asList(min, max));

    Attributes expectedAttributes =
        new Attributes().put(SERVICE_NAME, "fooService").put("specificKey", "specificValue");
    Summary expectedMetric =
        new Summary("metricName", 200, 123.55d, 5.5d, 100.01d, 9000L, 10000L, expectedAttributes);

    Collection<Metric> result =
        metricPointAdapter.buildMetricsFromPoint(
            metricDescriptor, Type.SUMMARY, commonAttributes, summary);

    assertEquals(singleton(expectedMetric), result);
  }
}
