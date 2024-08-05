package com.getlipa.eventstore.aggregate;

import com.getlipa.eventstore.event.EventMetadata;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.stream.AppendableStream;
import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Message;

public class Log<T> extends Aggregate<T> {

    private final AppendableStream appendableStream;

    public Log(
            final Id id,
            final Hydrator<T> hydrator,
            final EventTipHolder<T> eventTipHolder,
            final AppendableStream appendableStream
    ) {
        super(id, hydrator, eventTipHolder);
        this.appendableStream = appendableStream;
    }

    public static <T> Log<T> create(
            final Id id,
            final Hydrator<T> hydrator,
            final EventTipHolder<T> eventTipHolder,
            final AppendableStream appendableStream
    ) {
        return new Log<>(id, hydrator, eventTipHolder, appendableStream);
    }

    public <P extends Message> AppendableStream.Appender append() {
        return append(LogIndex.after(getRevision()));
    }

    public AppendableStream.Appender append(final LogIndex logIndex) {
        final var index = hydrator.initialized()
                .map(vd -> logIndex);
        return appendableStream.append(index)
                .onSuccessful(event -> hydrator.apply(event).mapEmpty());
    }

    @Override
    protected long extractRevision(EventMetadata eventMetadata) {
        return eventMetadata.getLogIndex();
    }

}
