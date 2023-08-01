package com.getlipa.eventstore.core.event;

import com.google.protobuf.Message;
import com.getlipa.eventstore.core.proto.AnyPayload;

public interface AnyEvent extends EventMetadata {

    AnyPayload getPayload();

    <T extends Message> AnyEvent on(Class<T> type, Handler<T> handler);

    interface Handler<T extends Message> {

        void handle(Event<T> subscriptionEvent);
    }
}
