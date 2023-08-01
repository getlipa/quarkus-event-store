package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.exception.DuplicateEventException;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.core.event.seriesindex.SeriesIndex;
import com.getlipa.eventstore.core.stream.selector.Events;
import com.getlipa.eventstore.example.event.Example;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;


@QuarkusTest
class PostgresEventPersistenceTest {


    @Inject
    private TransactionManager transactionManager;

    private PostgresEventPersistence persistence;

    @BeforeEach
    public void setup() {
        persistence = new PostgresEventPersistence();
        persistence.transactionManager = transactionManager;
    }

    @Test
    public void testAppend() throws EventAppendException {
        final var stream = Events.bySeries(UUID.randomUUID(), UUID.randomUUID());
        final var event = Example.Simple.newBuilder()
                .setData("some-dataaaaaaa")
                .build();
        final var first = persistence.append(stream, SeriesIndex.first(), Event.withPayload(event));
        final var second = persistence.append(stream, SeriesIndex.after(first), Event.withPayload(event));
        final var third = persistence.append(stream, SeriesIndex.after(second), Event.withPayload(event));

        Assertions.assertEquals(0, first.getSeriesIndex());
        Assertions.assertEquals(1, second.getSeriesIndex());
        Assertions.assertEquals(2, third.getSeriesIndex());
    }

    @Test
    public void testReAppend() throws EventAppendException {
        final var stream = Events.bySeries(UUID.randomUUID(), UUID.randomUUID());
        final var first = Event.withPayload(Example.Simple.newBuilder()
                .setData("first")
                .build());
        persistence.append(stream, SeriesIndex.atAny(), first);
        final var second = Event.withId(first.getId()).withPayload(Example.Simple.newBuilder()
                .setData("second")
                .build());
        Assertions.assertThrows(
                DuplicateEventException.class,
                () -> persistence.append(stream, SeriesIndex.atAny(), second)
        );
    }

    @Test
    public void testConflictingAppend() throws EventAppendException {
        final var stream = Events.bySeries(UUID.randomUUID(), UUID.randomUUID());
        final var first = Event.withPayload(Example.Simple.newBuilder()
                .setData("first")
                .build());
        persistence.append(stream, SeriesIndex.first(), first);
        final var replayed = Event.withPayload(Example.Simple.newBuilder()
                .setData("second")
                .build());
        Assertions.assertThrows(
                InvalidIndexException.class,
                () -> persistence.append(stream, SeriesIndex.first(), replayed)
        );

    }

}