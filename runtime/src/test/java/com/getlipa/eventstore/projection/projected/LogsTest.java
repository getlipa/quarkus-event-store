package com.getlipa.eventstore.projection.projected;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.Aggregates;
import com.getlipa.eventstore.aggregate.Logs;
import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.aggregate.context.NamedContext;
import com.getlipa.eventstore.aggregate.hydration.AggregateHydratorFactory;
import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogsTest {

    private static final NamedContext CONTEXT = new NamedContext("test");

    private static final Id ID = Id.derive("id");

    @Mock
    AggregateHydratorFactory aggregateHydratorFactory;

    @Mock
    EventStore eventStore;

    Logs<Object> aggregates;

    @BeforeEach
    void setUp() {
        doReturn(mock(Hydrator.class)).when(aggregateHydratorFactory).create(eq(ID), any(AggregateMiddleware.class));

        aggregates = new Logs<>(CONTEXT, aggregateHydratorFactory, eventStore);
    }

    @Test
    void get() {
        final var firstAggregate = aggregates.get(ID);
        final var secondAggregate = aggregates.get(ID);

        assertNotSame(firstAggregate, secondAggregate);
        verify(aggregateHydratorFactory, times(2)).create(eq(ID), any(AggregateMiddleware.class));
        verify(eventStore, times(2)).stream(CONTEXT.createSelector(ID));
    }
}