package com.newrelic.telemetry.opentelemetry.export;

import static com.newrelic.telemetry.opentelemetry.export.AttributeNames.INSTRUMENTATION_NAME;
import static com.newrelic.telemetry.opentelemetry.export.AttributeNames.INSTRUMENTATION_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.ReadableAttributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class AttributesSupportTest {

  @Test
  void populateLibraryInfo_nullInstrumentationLibrary() {
    Attributes attributes = new Attributes().put("foo", "bar");
    Attributes result = AttributesSupport.populateLibraryInfo(attributes, null);
    assertSame(attributes, result);
  }

  @Test
  void populateLibraryInfo_happyPath() {
    Attributes attributes = new Attributes().put("foo", "bar");
    Attributes expected =
        attributes.put(INSTRUMENTATION_NAME, "theName").put(INSTRUMENTATION_VERSION, "theVersion");
    InstrumentationLibraryInfo libraryInfo = mock(InstrumentationLibraryInfo.class);

    when(libraryInfo.getName()).thenReturn("theName");
    when(libraryInfo.getVersion()).thenReturn("theVersion");

    Attributes result = AttributesSupport.populateLibraryInfo(attributes, libraryInfo);
    assertEquals(expected, result);
  }

  @Test
  void populateLibraryInfo_nullsAndEmpty() {
    Attributes attributes = new Attributes().put("foo", "bar");
    Attributes expected = attributes.copy();
    InstrumentationLibraryInfo libraryInfo = mock(InstrumentationLibraryInfo.class);

    when(libraryInfo.getName()).thenReturn(null);
    when(libraryInfo.getVersion()).thenReturn("");

    Attributes result = AttributesSupport.populateLibraryInfo(attributes, libraryInfo);
    assertEquals(expected, result);
  }

  @Test
  void addResourceAttributes_nullResource() {
    Attributes attributes = new Attributes().put("a", "b");
    Attributes result = AttributesSupport.addResourceAttributes(attributes, null);
    assertSame(attributes, result);
  }

  @Test
  void addResourceAttributes_happyPath() {
    Attributes attributes = new Attributes().put("a", "b");
    ReadableAttributes resourceAttributes =
        io.opentelemetry.common.Attributes.of(
            "r1", AttributeValue.stringAttributeValue("v1"),
            "r2", AttributeValue.longAttributeValue(23),
            "r3", AttributeValue.booleanAttributeValue(true));
    Attributes expected =
        new Attributes().put("a", "b").put("r1", "v1").put("r2", 23L).put("r3", true);
    Resource resource = mock(Resource.class);
    when(resource.getAttributes()).thenReturn(resourceAttributes);
    Attributes result = AttributesSupport.addResourceAttributes(attributes, resource);
    assertEquals(expected, result);
  }

  @Test
  void addResourceAttributes_resourceWinsVsInput() {
    Attributes attributes = new Attributes().put("r1", "v77");
    ReadableAttributes resourceAttributes =
        io.opentelemetry.common.Attributes.of(
            "r1", AttributeValue.stringAttributeValue("v1"),
            "r2", AttributeValue.longAttributeValue(23),
            "r3", AttributeValue.booleanAttributeValue(true));
    Attributes expected = new Attributes().put("r1", "v1").put("r2", 23L).put("r3", true);
    Resource resource = mock(Resource.class);
    when(resource.getAttributes()).thenReturn(resourceAttributes);
    Attributes result = AttributesSupport.addResourceAttributes(attributes, resource);
    assertEquals(expected, result);
  }

  @Test
  void putInAttributes_allTypes() {
    Attributes attrs = new Attributes().put("y", "z");
    AttributeValue superBrokenValue = AttributeValue.arrayAttributeValue(12d, 11d, 9d);

    ReadableAttributes original =
        io.opentelemetry.common.Attributes.of(
            "r1", AttributeValue.stringAttributeValue("v1"),
            "r2", AttributeValue.longAttributeValue(23),
            "r3", AttributeValue.booleanAttributeValue(true),
            "r4", AttributeValue.doubleAttributeValue(23.7),
            "r5", superBrokenValue);
    Attributes expected =
        attrs.copy().put("r1", "v1").put("r2", 23L).put("r3", true).put("r4", 23.7d);
    AttributesSupport.putInAttributes(attrs, original);

    assertEquals(expected, attrs);
  }
}
