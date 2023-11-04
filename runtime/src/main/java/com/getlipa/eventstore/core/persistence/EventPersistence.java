package com.getlipa.eventstore.core.persistence;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.stream.reader.ReadOptions;
import com.google.protobuf.Message;
import io.vertx.core.Future;

import java.util.Iterator;
import java.util.UUID;

public interface EventPersistence {


    <T extends Message> Future<AnyEvent> append(
            ByLogSelector selector,
            LogIndex logIndex,
            EphemeralEvent<T> event
    );

    Future<Iterator<AnyEvent>> read(Selector selector, final ReadOptions readOptions);

    Future<AnyEvent> read(UUID id);
}
