package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.core.stream.selector.ByCausationIdSelector;
import com.getlipa.eventstore.core.stream.selector.ByCorrelationIdSelector;
import com.getlipa.eventstore.core.stream.selector.BySeriesTypeSelector;
import com.getlipa.eventstore.core.stream.selector.ByTypeSelector;
import com.getlipa.eventstore.core.event.seriesindex.SeriesIndex;
import com.getlipa.eventstore.core.proto.PayloadParser;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.core.stream.options.ReadOptions;
import com.getlipa.eventstore.core.stream.selector.ByStreamSelector;
import com.google.protobuf.Message;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.RollbackException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Iterator;

@ApplicationScoped
@RequiredArgsConstructor
public class PostgresEventPersistence extends JtaEventPersistence<JpaEvent> {

    private final PayloadParser parser;

    @Override
    protected void handleRollback(RollbackException rollbackException) throws EventAppendException {
        if (! (rollbackException.getCause() instanceof ConstraintViolationException)) {
            throw EventAppendException.from(rollbackException);
        }
        final var violation = (ConstraintViolationException) rollbackException.getCause();

        // TODO
        switch (violation.getConstraintName()) {
            case "event_id_unique":
                throw EventAppendException.duplicateEvent(violation);
            case "consecutive_series_index":
                throw InvalidIndexException.nonConsecutiveIndex(null);
            default:
                throw EventAppendException.from(violation);
        }
    }

    @Override
    protected <T extends Message> Event<T> doAppend(
            ByStreamSelector selector,
            SeriesIndex seriesIndex,
            EphemeralEvent<T> event
    ) {
        final var jpaEvent = JpaEvent.builder(event)
                .seriesIndex(seriesIndex.getValue())
                .seriesType(ProtoUtil.toUUID(selector.getSeriesType()))
                .seriesId(selector.getSeriesId())
                .build();
        jpaEvent.persist();
        return Event.<T>from(jpaEvent)
                .payload(event::payload)
                .build();
    }

    @Override
    public Iterator<Event<Message>> read(ByStreamSelector selector, ReadOptions readOptions) {
        final var query = EventQuery.create(selector, readOptions);
        return JpaEvent.<JpaEvent>find(query.getQuery(), query.getSort(), query.getParameters())
                .range(0, readOptions.limit())
                .stream()
                .map(jpaEvent -> jpaEvent.toPersistedEvent(parser))
                .iterator();
    }

    @Override
    public Iterator<Event<Message>> read(BySeriesTypeSelector selector, ReadOptions readOptions) {
        return null;
    }

    @Override
    public Iterator<Event<Message>> read(ByCausationIdSelector selector, ReadOptions readOptions) {
        return null;
    }

    @Override
    public Iterator<Event<Message>> read(ByCorrelationIdSelector selector, ReadOptions readOptions) {
        return null;
    }

    @Override
    public Iterator<Event<Message>> read(ByTypeSelector selector, ReadOptions readOptions) {
        return null;
    }
}
