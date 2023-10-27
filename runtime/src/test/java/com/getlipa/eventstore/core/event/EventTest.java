package com.getlipa.eventstore.core.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    public void correctEventIdNamespaceValue() {
        Assertions.assertEquals("$event-id", Event.EVENT_ID_NAMESPACE);
    }

    @Test
    public void correctEventTypeNamespaceValue() {
        Assertions.assertEquals("$event-type", Event.EVENT_TYPE_NAMESPACE);
    }

    @Test
    public void correctEventSeriesTypeNamespaceValue() {
        Assertions.assertEquals("$event-series-type", Event.EVENT_SERIES_TYPE_NAMESPACE);
    }

    @Test
    public void correctEventSeriesIdNamespaceValue() {
        Assertions.assertEquals("$event-series-id", Event.EVENT_SERIES_ID_NAMESPACE);
    }
    @Test
    public void correctEventCorrelationIdNamespaceValue() {
        Assertions.assertEquals("$event-correlation-id", Event.EVENT_CORRELATION_ID_NAMESPACE);
    }
}
