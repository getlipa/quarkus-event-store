package com.getlipa.eventstore.core.persistence;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.Events;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.core.persistence.inmemory.InMemoryEventPersistence;
import com.getlipa.eventstore.core.stream.reader.cursor.Cursor;
import com.getlipa.eventstore.core.stream.reader.Direction;
import com.getlipa.eventstore.core.stream.reader.ReadOptions;
import com.getlipa.eventstore.example.event.Example;
import com.google.protobuf.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Stream;


// TODO: Use as generic Test Set for every implementation of EventPersistence?
class EventPersistenceTest {

    private EventPersistence persistence;

    @BeforeEach
    public void setup() {
        persistence = new InMemoryEventPersistence();
    }

    @Test
    public void testAppend() throws Throwable {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var first = append(stream, LogIndex.first(), "first");
        final var second = append(stream, LogIndex.after(first), "second");
        final var third = append(stream, LogIndex.after(second), "third");

        Assertions.assertEquals(0, first.getLogIndex());
        Assertions.assertEquals(1, second.getLogIndex());
        Assertions.assertEquals(2, third.getLogIndex());

        Assertions.assertTrue(0 < first.getPosition());
        Assertions.assertTrue(first.getPosition() < second.getPosition());
        Assertions.assertTrue(second.getPosition() < third.getPosition());
    }

    @Test
    public void testAppendAny() throws Throwable {
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
    public void testReAppend() throws Throwable {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var first = Event.withPayload(Example.Simple.newBuilder()
                .setData("first")
                .build());
        final var firstAppended = append(stream, LogIndex.atAny(), first);
        final var second = Event.withId(first.getId()).withPayload(Example.Simple.newBuilder()
                .setData("second")
                .build());
        final var secondAppended = append(stream, LogIndex.atAny(), second);
        Assertions.assertSame(firstAppended, secondAppended);
    }

    @Test
    public void testConflictingAppend() throws Throwable {
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
                () -> append(stream, LogIndex.first(), replayed)
        );
    }

    private static Stream<Arguments> testRead() {
        return Stream.of(
                Arguments.of(Events.all(), ReadOptions.builder().limit(2).build(), "a-0-0 a-0-1"),
                Arguments.of(Events.all(), ReadOptions.builder().from(Cursor.position(4)).build(), "b-1-1 c-0-0 c-1-0"),
                Arguments.of(Events.all(), ReadOptions.builder().until(Cursor.position(4)).build(), "a-0-0 a-0-1 b-1-0"),
                Arguments.of(Events.byLogId("inexistent"), ReadOptions.DEFAULT, ""),
                Arguments.of(Events.byLogDomain("c"), ReadOptions.DEFAULT, "c-0-0 c-1-0"),
                Arguments.of(Events.byLogDomain("c"), ReadOptions.builder().direction(Direction.BACKWARD).build(), "c-1-0 c-0-0"),
                Arguments.of(Events.byLogId(id(0)), ReadOptions.DEFAULT, "a-0-0 a-0-1 c-0-0"),
                Arguments.of(Events.byLog("a", id(0)), ReadOptions.DEFAULT, "a-0-0 a-0-1"),
                Arguments.of(Events.byCorrelationId(id(5)), ReadOptions.DEFAULT, "b-1-0 c-0-0")
        );
    }

    @MethodSource
    @ParameterizedTest
    public void testRead(Selector selector, ReadOptions readOptions, String expected) throws Throwable {
        append(Events.byLog("a", id(0)), "");
        append(Events.byLog("a", id(0)), "");
        append(Events.byLog("b", id(1)), event -> event.withCorrelationId(id(5)));
        append(Events.byLog("b", id(1)), event -> event.withCausationId(id(5)));
        append(Events.byLog("c", id(0)), event -> event.withCorrelationId(id(5)));
        append(Events.byLog("c", id(1)), event -> event.withCausationId(id(5)));

        final var stringBuilder = new StringBuilder();
        persistence.read(selector, readOptions).toCompletionStage().toCompletableFuture().join()
                .forEachRemaining(event -> stringBuilder.append(String.format(
                        "%s-%s-%s ",
                        event.getLogDomain(),
                        event.getLogId().toString().replaceAll("[0-](?=.)", ""),
                        event.getLogIndex()
                )));
        Assertions.assertEquals(expected, stringBuilder.toString().trim());
    }

    static UUID id(int id) {
        long mostSigBits = 0L; // All zeros for the most significant bits
        long leastSigBits = id & 0xFFFFFFFFFFFFL; // Ensure the integer fits in 48 bits
        return new UUID(mostSigBits, leastSigBits);
    }

    AnyEvent append(
            final ByLogSelector selector,
            final Function<EphemeralEvent.Builder, EphemeralEvent.Builder> event
    ) throws Throwable {
        return append(
                selector,
                event,
                Example.Simple.newBuilder().build()
        );
    }

    <T extends Message> AnyEvent append(
            final ByLogSelector selector,
            final Function<EphemeralEvent.Builder, EphemeralEvent.Builder> event,
            final T payload
    ) throws Throwable {
        return append(
                selector,
                LogIndex.atAny(),
                event.apply(EphemeralEvent.create()).withPayload(payload)
        );
    }

    <T extends Message> AnyEvent append(
            final ByLogSelector selector,
            final String data
    ) throws Throwable {
        return append(selector, LogIndex.atAny(), data);
    }

    <T extends Message> AnyEvent append(
            final ByLogSelector selector,
            final LogIndex logIndex,
            final String data
    ) throws Throwable {
        final var event = Event.withPayload(Example.Simple.newBuilder()
                .setData(data)
                .build());
        return append(selector, logIndex, event);
    }

    <T extends Message> AnyEvent append(
            final ByLogSelector selector,
            final LogIndex logIndex,
            final EphemeralEvent<T> event
    ) throws Throwable {
        try {

            return persistence.append(selector, logIndex, event)
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }
}