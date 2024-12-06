package com.getlipa.eventstore.persistence;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.EphemeralEvent;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.stream.reader.ReadOptions;
import com.google.protobuf.Message;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Future;

import java.util.Iterator;

public interface EventPersistence {


    <T extends Message> Uni<AnyEvent> append(
            ByLogSelector selector,
            LogIndex logIndex,
            EphemeralEvent<T> event
    );

    Multi<AnyEvent> read(Selector selector, final ReadOptions readOptions);

    Uni<AnyEvent> read(Id id);
}
