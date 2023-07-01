package com.getlipa.eventstore.core.event;

import com.google.protobuf.Message;

public interface AnyEvent extends EventMetadata {

    Message payload();

    <T extends Message> AnyEvent on(Class<T> type, Handler<T> handler);

    interface Handler<T extends Message> {

        void handle(Event<T> subscriptionEvent);
    }
}
