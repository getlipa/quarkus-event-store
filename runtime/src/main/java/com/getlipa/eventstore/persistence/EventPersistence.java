package com.getlipa.eventstore.persistence;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.EphemeralEvent;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.stream.reader.ReadOptions;
import com.google.protobuf.Message;
import io.vertx.core.Future;

import java.util.Iterator;

public interface EventPersistence {


    <T extends Message> Future<AnyEvent> append(
            ByLogSelector selector,
            LogIndex logIndex,
            EphemeralEvent<T> event
    );

    Future<Iterator<AnyEvent>> read(Selector selector, final ReadOptions readOptions);

    Future<AnyEvent> read(Id id);
}
