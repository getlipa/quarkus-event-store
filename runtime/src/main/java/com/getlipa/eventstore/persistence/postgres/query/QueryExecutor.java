package com.getlipa.eventstore.persistence.postgres.query;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.persistence.postgres.EventQuery;
import com.getlipa.eventstore.persistence.postgres.JpaEvent;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@RequiredArgsConstructor
public class QueryExecutor {

    @Transactional
    public List<AnyEvent> execute(EventQuery query) {
        return JpaEvent.<JpaEvent>find(query.getQuery(), query.getSort(), query.getParameters())
                .range(0, query.getLimit() - 1)
                .stream()
                .map(JpaEvent::toPersistedEvent)
                .collect(Collectors.toList());
    }

    @Transactional
    public AnyEvent find(Id id) {
        return JpaEvent.<JpaEvent>find("uuid = :id", Parameters.with("id", id.toUuid()))
                .firstResult()
                .toPersistedEvent();
    }
}
