package com.getlipa.eventstore.core;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.event.Events;
import com.getlipa.eventstore.core.event.logindex.LogIndex;
import com.getlipa.eventstore.core.event.selector.ByLogSelector;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.persistence.exception.InvalidIndexException;
import com.getlipa.eventstore.core.persistence.inmemory.InMemoryEventPersistence;
import com.getlipa.eventstore.core.stream.reader.cursor.Cursor;
import com.getlipa.eventstore.core.stream.reader.EventReader;
import com.getlipa.eventstore.example.event.Example;
import com.google.protobuf.Message;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventStoreTest {

    private EventStore eventStore;

    private EventPersistence persistence;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setup() {
        persistence = spy(new InMemoryEventPersistence());
        final var observer = mock(jakarta.enterprise.event.Event.class);
        doReturn(Future.succeededFuture().toCompletionStage()).when(observer).fireAsync(any());
        eventStore = new EventStore(Vertx.vertx(), persistence, observer);
    }

    @Test
    public void testAppend() throws Throwable {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var first = append(stream, LogIndex.first());
        final var second = append(stream, LogIndex.after(first));
        final var third = append(stream, LogIndex.after(second));

        assertEquals(0, first.getLogIndex());
        assertEquals(1, second.getLogIndex());
        assertEquals(2, third.getLogIndex());

        Assertions.assertTrue(0 < first.getPosition());
        Assertions.assertTrue(first.getPosition() < second.getPosition());
        Assertions.assertTrue(second.getPosition() < third.getPosition());
    }

    @Test
    public void testAppendAny() {
        final var stream = Events.byLog(UUID.randomUUID(), UUID.randomUUID());
        final var event = Example.Simple.newBuilder()
                .setData("some-data")
                .build();
        final var first = eventStore.stream(stream)
                .append(LogIndex.atAny(), Event.withPayload(event))
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        assertEquals(0, first.getLogIndex());
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
        eventStore.stream(stream)
                .append(LogIndex.first(), first)
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

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ReadTest {

        @BeforeEach
        void setupTestData() throws Throwable {
            append(Events.byLog("a", id(0)));
            append(Events.byLog("a", id(0)));
            append(Events.byLog("b", id(1)), event -> event.withCorrelationId(id(5)));
            append(Events.byLog("b", id(1)), event -> event.withCausationId(id(5)));
            append(Events.byLog("c", id(0)), event -> event.withCorrelationId(id(5)));
            append(Events.byLog("c", id(1)), event -> event.withCausationId(id(5)));
            for (var i = 0; i < 2000; i++) {
                append(Events.byLog("d", id(7)));
            }
        }

        Stream<Arguments> testRead() {
            return Stream.of(
                    test(es -> es.stream(Events.all()).readForward().limit(2), "a-0-0 a-0-1"),
                    test(es -> es.stream(Events.all()).readForward().from(Cursor.position(4)).limit(3), "b-1-1 c-0-0 c-1-0"),
                    test(es -> es.stream(Events.all()).readForward().until(Cursor.position(4)), "a-0-0 a-0-1 b-1-0 b-1-1"),
                    test(es -> es.stream(Events.byLogId("inexistent")).readForward(), ""),
                    test(es -> es.stream(Events.byLogDomain("c")).readForward(), "c-0-0 c-1-0"),
                    test(es -> es.stream(Events.byLogDomain("c")).readBackward(), "c-1-0 c-0-0"),
                    test(es -> es.stream(Events.byLogId(id(0))).readForward(), "a-0-0 a-0-1 c-0-0"),
                    test(es -> es.stream(Events.byLog("a", id(0))).readForward(), "a-0-0 a-0-1"),
                    test(es -> es.stream(Events.byCorrelationId(id(5))).readForward(), "b-1-0 c-0-0")
            );
        }

        @MethodSource
        @ParameterizedTest
        public void testRead(Function<EventStore, EventReader> readerProvider, String expected) {
            final var stringBuilder = new StringBuilder();
            readerProvider.apply(eventStore)
                    .forEach(event -> Future.succeededFuture(stringBuilder.append(String.format(
                            "%s-%s-%s ",
                            event.getLogDomain(),
                            event.getLogId().toString().replaceAll("[0-](?=.)", ""),
                            event.getLogIndex()
                    ))))
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();
            assertEquals(expected, stringBuilder.toString().trim());
        }

        @Test
        public void testReadWithDelayedHandler() {
            final var results = new LinkedList<String>();
            final var isFirst = new AtomicBoolean(true);
            eventStore.stream(Events.all())
                    .readForward()
                    .limit(2)
                    .forEach((event) -> {
                        final var result = Promise.promise();
                        result.future().onComplete(vd -> results.add(String.valueOf(event.getPosition())));
                        if (isFirst.get()) {
                            Vertx.vertx().setTimer(100, id -> result.complete());
                            isFirst.set(false);
                        } else {
                            result.complete();
                        }
                        return result.future();
                    })
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();

            assertEquals("1 2", String.join(" ", results));
        }


        Stream<Arguments> testReadPaged() {
            return Stream.of(
                    // pageSize | from | limit | expectedPages | expectedEvents
                    Arguments.of(1, 3, 10, 10, "3_4_5_6_7_8_9_10_11_12"),
                    Arguments.of(2, 3, 10, 5, "3_4_5_6_7_8_9_10_11_12"),
                    Arguments.of(10, 0, 100, 10, "1_.*_100"),
                    Arguments.of(1, 0, 10000, 2007, "1_.*_2006"),
                    Arguments.of(10, 0, 101, 11, "1_.*_101"),
                    Arguments.of(10, 0, 102, 11, "1_.*_102")
            );
        }

        @ParameterizedTest
        @MethodSource("testReadPaged")
        public void testReadPagedForward(int pageSize, int startAt, int limit, int expectedPages, String expectedEventsRead) throws Throwable {
            final var stringJoiner = new StringJoiner("_");
            final var selector = Events.all();
            eventStore.stream(selector)
                    .readForward()
                    .limit(limit)
                    .from(Cursor.position(startAt))
                    .pageSize(pageSize)
                    .forEach(event -> {
                        stringJoiner.add(String.valueOf(event.getPosition()));
                        return Future.succeededFuture();
                    })
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();
            assertLinesMatch(List.of(expectedEventsRead), List.of(stringJoiner.toString()));
            verify(persistence, times(expectedPages)).read(same(selector), any());
        }

        @ParameterizedTest
        @MethodSource("testReadPaged")
        public void testReadPagedBackward(int pageSize, int startAt, int limit, int expectedPages, String expectedEventsRead) throws Throwable {
            final var selector = Events.all();
            final var startPosition = eventStore.stream(selector)
                    .readForward()
                    .limit(limit)
                    .from(Cursor.position(startAt))
                    .aggregate(Long.MAX_VALUE, (aggregated, event) -> Future.succeededFuture(event.getPosition()))
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();

            reset(persistence);
            final var results = new LinkedList<String>();
            eventStore.stream(selector)
                    .readBackward()
                    .limit(limit)
                    .from(Cursor.position(startPosition))
                    .pageSize(pageSize)
                    .forEach(event -> {
                        results.add(String.valueOf(event.getPosition()));
                        return Future.succeededFuture();
                    })
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();
            Collections.reverse(results);
            assertLinesMatch(
                    List.of(expectedEventsRead),
                    List.of(String.join("_", results))
            );
            verify(persistence, times(expectedPages)).read(same(selector), any());
        }

    }

    static UUID id(int id) {
        long mostSigBits = 0L; // All zeros for the most significant bits
        long leastSigBits = id & 0xFFFFFFFFFFFFL; // Ensure the integer fits in 48 bits
        return new UUID(mostSigBits, leastSigBits);
    }

    AnyEvent append(
            final ByLogSelector selector,
            final LogIndex cursor
    ) throws Throwable {
        return append(
                selector,
                cursor,
                Event.withPayload(Example.Simple.newBuilder().build())
        );
    }

    AnyEvent append(
            final ByLogSelector selector,
            final Function<EphemeralEvent.Builder, EphemeralEvent.Builder> event
    ) throws Throwable {
        return append(
                selector,
                LogIndex.atAny(),
                event,
                Example.Simple.newBuilder().build()
        );
    }

    <T extends Message> AnyEvent append(
            final ByLogSelector selector,
            final LogIndex cursor,
            final Function<EphemeralEvent.Builder, EphemeralEvent.Builder> event,
            final T payload
    ) throws Throwable {
        return append(
                selector,
                cursor,
                event.apply(EphemeralEvent.create()).withPayload(payload)
        );
    }

    AnyEvent append(final ByLogSelector selector) throws Throwable {
        return append(selector, event -> event);
    }

    <T extends Message> AnyEvent append(
            final ByLogSelector selector,
            final LogIndex logIndex,
            final EphemeralEvent<T> event
    ) throws Throwable {
        try {

            return eventStore.stream(selector)
                    .append(logIndex, event)
                    .toCompletionStage()
                    .toCompletableFuture()
                    .join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }

    static Arguments test(Function<EventStore, EventReader> readerProvider, String expected) {
        return Arguments.of(readerProvider, expected);
    }
}