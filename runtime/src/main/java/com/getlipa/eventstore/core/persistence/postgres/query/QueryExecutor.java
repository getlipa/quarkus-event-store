package com.getlipa.eventstore.core.persistence.postgres.query;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.postgres.EventQuery;
import com.getlipa.eventstore.core.persistence.postgres.JpaEvent;
import com.google.protobuf.Message;
import io.quarkus.panache.common.Parameters;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class QueryExecutor {

    @Transactional
    public Future<Long> execute(final EventQuery query, final Event.Handler<Message> handler) {
        final var result = JpaEvent.<JpaEvent>find(query.getQuery(), query.getSort(), query.getParameters())
                .page(0, 100);
        var completed = Future.succeededFuture(-1L);
        while (result.hasNextPage() && !completed.failed()) {
            final var page = result.nextPage();
            for (final var event : page.list()) {
                completed = completed
                        .flatMap(vd -> handler.handle(event.toPersistedEvent()))
                        .map(vd -> event.getPosition());
            }
        }
        return completed;
    }

    @Transactional
    public List<AnyEvent> execute(EventQuery query) {
        /*
        TODO: Use
         */
        return JpaEvent.<JpaEvent>find(query.getQuery(), query.getSort(), query.getParameters())
                .page(0, 100)
                .stream()
                .map(JpaEvent::toPersistedEvent)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnyEvent find(UUID id) {
        return JpaEvent.<JpaEvent>find("id = :id", Parameters.with("id", id))
                .firstResult()
                .toPersistedEvent();
    }
}
