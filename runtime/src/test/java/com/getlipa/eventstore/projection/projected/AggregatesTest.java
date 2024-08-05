package com.getlipa.eventstore.projection.projected;

import com.getlipa.eventstore.aggregate.hydration.AggregateHydratorFactory;
import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.aggregate.Aggregate;
import com.getlipa.eventstore.aggregate.Aggregates;
import com.getlipa.eventstore.projection.ProjectionMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AggregatesTest {

    private static final Id ID = Id.derive("id");

    @Mock
    AggregateHydratorFactory aggregateHydratorFactory;

    Aggregates<Object> aggregates;

    @BeforeEach
    void setUp() {
        doReturn(mock(Hydrator.class)).when(aggregateHydratorFactory).create(eq(ID), any(AggregateMiddleware.class));

        aggregates = new Aggregates<>(aggregateHydratorFactory);
    }

    @Test
    void get() {
        final var firstAggregate = aggregates.get(ID);
        final var secondAggregate = aggregates.get(ID);

        assertNotSame(firstAggregate, secondAggregate);
        verify(aggregateHydratorFactory, times(2)).create(eq(ID), any(AggregateMiddleware.class));
    }
}