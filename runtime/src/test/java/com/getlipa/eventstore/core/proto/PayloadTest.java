package com.getlipa.eventstore.core.proto;

import com.getlipa.eventstore.core.event.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PayloadTest {

    @Test
    public void causationEventCorrelationIdNamespaceValue() {
        Assertions.assertEquals("$payload-type", Payload.PAYLOAD_TYPE_NAMESPACE);
    }
}
