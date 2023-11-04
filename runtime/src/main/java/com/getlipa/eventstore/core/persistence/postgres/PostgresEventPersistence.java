package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.core.persistence.postgres.query.QueryExecutor;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.core.stream.reader.ReadOptions;
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
                throw InvalidIndexException.nonConsecutiveIndex(null);
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
                .logDomainUuid(ProtoUtil.toUUID(Event.EVENT_LOG_DOMAIN_NAMESPACE, selector.getLogDomain()))
                .logDomain(selector.getLogDomain())
                .logId(selector.getLogId())
                .build();
        jpaEvent.persist();
    }

    @Override
    public Future<AnyEvent> read(UUID id) {
        return vertx.executeBlocking(result -> result.complete(queryExecutor.find(id)));
    }

    @Override
    public Future<Iterator<AnyEvent>> read(Selector selector, final ReadOptions readOptions) {
        final var query = EventQuery.create(selector, readOptions);

        return vertx.executeBlocking(result -> result.complete(queryExecutor.execute(query).iterator()));
    }
}
