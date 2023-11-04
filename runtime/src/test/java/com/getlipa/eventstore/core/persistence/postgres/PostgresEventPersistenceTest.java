package com.getlipa.eventstore.core.persistence.postgres;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.exception.DuplicateEventException;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.event.Events;
import com.getlipa.eventstore.core.persistence.postgres.query.QueryExecutor;
import com.getlipa.eventstore.example.event.Example;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import jakarta.transaction.TransactionManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;


@QuarkusTest
class PostgresEventPersistenceTest {

    @Inject
    TransactionManager transactionManager;

    @Inject
    Vertx vertx;

    @Inject
    QueryExecutor queryExecutor;

    private PostgresEventPersistence persistence;

    @BeforeEach
    public void setup() {
        persistence = new PostgresEventPersistence();
        persistence.transactionManager = transactionManager;
        persistence.vertx = vertx;
        persistence.queryExecutor = queryExecutor;
    }

    @Test
    public void testAppend() throws EventAppendException {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var event = Example.Simple.newBuilder()
                .setData("some-data")
                .build();
        final var first = persistence.append(stream, LogIndex.first(), Event.withPayload(event))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
        final var second = persistence.append(stream, LogIndex.after(first), Event.withPayload(event))
                .toCompletionStage()
                .toCompletableFuture()
                .join();
        final var third = persistence.append(stream, LogIndex.after(second), Event.withPayload(event))
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        Assertions.assertEquals(0, first.getLogIndex());
        Assertions.assertEquals(1, second.getLogIndex());
        Assertions.assertEquals(2, third.getLogIndex());

        Assertions.assertTrue(0 < first.getPosition());
        Assertions.assertTrue(first.getPosition() < second.getPosition());
        Assertions.assertTrue(second.getPosition() < third.getPosition());
    }

    @Test
    public void testAppendAny() throws EventAppendException {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var event = Example.Simple.newBuilder()
                .setData("some-data")
                .build();
        final var first = persistence.append(stream, LogIndex.atAny(), Event.withPayload(event))
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        Assertions.assertEquals(0, first.getLogIndex());
    }

    @Test
    public void testReAppend() throws EventAppendException {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var first = Event.withPayload(Example.Simple.newBuilder()
                .setData("first")
                .build());
        persistence.append(stream, LogIndex.atAny(), first)
                .toCompletionStage()
                .toCompletableFuture()
                .join();
        final var second = Event.withId(first.getId()).withPayload(Example.Simple.newBuilder()
                .setData("second")
                .build());
        Assertions.assertThrows(
                DuplicateEventException.class,
                () -> persistence.appendBlocking(stream, LogIndex.atAny(), second)
        );
    }

    @Test
    public void testConflictingAppend() throws EventAppendException {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var first = Event.withPayload(Example.Simple.newBuilder()
                .setData("first")
                .build());
        persistence.append(stream, LogIndex.first(), first)
                .toCompletionStage()
                .toCompletableFuture()
                .join();
        final var replayed = Event.withPayload(Example.Simple.newBuilder()
                .setData("second")
                .build());
        Assertions.assertThrows(
                InvalidIndexException.class,
                () -> persistence.appendBlocking(stream, LogIndex.first(), replayed)
        );
    }
}
