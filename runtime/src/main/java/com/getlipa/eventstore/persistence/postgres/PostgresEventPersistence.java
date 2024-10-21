package com.getlipa.eventstore.persistence.postgres;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.EphemeralEvent;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.persistence.exception.EventAppendException;
import com.getlipa.eventstore.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.persistence.postgres.query.QueryExecutor;
import com.getlipa.eventstore.stream.reader.ReadOptions;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.RollbackException;
import org.hibernate.exception.ConstraintViolationException;

import java.util.*;


@ApplicationScoped
public class PostgresEventPersistence extends JtaEventPersistence<JpaEvent> {

    @Inject
    QueryExecutor queryExecutor;

    @Override
    protected void handleRollback(RollbackException rollbackException) throws EventAppendException {
        if (!(rollbackException.getCause() instanceof ConstraintViolationException)) {
            throw EventAppendException.from(rollbackException);
        }
        final var violation = (ConstraintViolationException) rollbackException.getCause();

        // TODO
        switch (violation.getConstraintName()) {
            case "event_id_unique":
                throw EventAppendException.duplicateEvent(violation);
            case "consecutive_log_index":
                throw InvalidIndexException.nonConsecutiveIndex(null); // FIXME
            default:
                throw EventAppendException.from(violation);
        }
    }

    @Override
    protected <P extends Message> void doAppend(
            ByLogSelector selector,
            LogIndex logIndex,
            EphemeralEvent<P> event
    ) {
        final var jpaEvent = JpaEvent.builder(event)
                .logIndex(logIndex.getValue())
                .logDomainUuid(JpaEvent.createLogDomainId(selector.getContext()))
                .logContext(selector.getContext())
                .logUuid(selector.getLogId().toUuid())
                .build();
        jpaEvent.persist();
    }

    @Override
    public Future<AnyEvent> read(Id id) {
        return vertx.executeBlocking(result -> result.complete(queryExecutor.find(id)), false);
    }

    @Override
    public Future<Iterator<AnyEvent>> read(Selector selector, final ReadOptions readOptions) {
        final var query = EventQuery.create(selector, readOptions);

        return vertx.executeBlocking(result -> result.complete(queryExecutor.execute(query).iterator()), false);
    }
}
