package com.getlipa.eventstore.stream.reader;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.persistence.EventPersistence;
import com.getlipa.eventstore.stream.reader.cursor.Cursor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class Paginator {

    private final Vertx vertx;

    private final EventPersistence eventPersistence;

    private final Selector selector;

    private final ReadOptions readOptions;

    public static Paginator create(
            final Vertx vertx,
            final EventPersistence eventPersistence,
            final Selector selector,
            final ReadOptions readOptions
    ) {
        return new Paginator(
                vertx,
                eventPersistence,
                selector,
                readOptions
        );
    }

    public Future<Long> forEach(AnyEvent.Handler<?> eventHandler) {
        final var result = Promise.<Long>promise();
        Multi.createFrom().<AnyEvent>emitter(this::emitEvents)
                .concatMap(event -> Multi.createFrom()
                        .completionStage(eventHandler.handle(event)
                                .map(vd -> event.getPosition())
                                .toCompletionStage()))
                .collect()
                .with(Collectors.summarizingLong(Long::longValue))
                .subscribe()
                .with(stats -> result.complete(stats.getCount()), result::fail);
        return result.future();
    }

    Future<Iterator<AnyEvent>> loadPage(final int numOfProcessedEvents, final Cursor currentPosition) {
        if (numOfProcessedEvents >= readOptions.limit()) {
            return Future.succeededFuture(Collections.emptyIterator());
        }
        return eventPersistence.read(selector, ReadOptions.from(readOptions)
                        .from(currentPosition)
                        .limit(readOptions.pageSize() + (numOfProcessedEvents > 0 ? 1 : 0))
                        .build())
                .map(page -> {
                    if (numOfProcessedEvents > 0 && page.hasNext() && !currentPosition.pointsTo(page.next())) {
                        throw new IllegalStateException("Stream was truncated during read."); // FIXME
                    }
                    return page;
                })
                .onFailure(error -> log.error("Error reading stream: {}", error.getMessage()));
    }

    void emitEvents(MultiEmitter<? super AnyEvent> emitter) {
        final var currentPosition = new AtomicReference<>(readOptions.from());
        final var numOfProcessedEvents = new AtomicInteger(0);
        vertx.executeBlocking(() -> {
                    Future<Cursor> currentPage = null;
                    while (!emitter.isCancelled()) {
                        if (currentPage != null && !currentPage.isComplete()) {
                            continue;
                        }
                        currentPage = loadPage(numOfProcessedEvents.get(), currentPosition.get())
                                .flatMap(iterator -> {
                                    if (!iterator.hasNext()) {
                                        emitter.complete();
                                        return Future.succeededFuture(currentPosition.get());
                                    }
                                    while (iterator.hasNext() && numOfProcessedEvents.get() < readOptions.limit()) {
                                        final var event = iterator.next();
                                        currentPosition.set(Cursor.position(event));
                                        numOfProcessedEvents.incrementAndGet();
                                        emitter.emit(event);
                                    }
                                    return Future.succeededFuture(currentPosition.get());
                                })
                                .onFailure(emitter::fail);
                    }
                    return new Void[]{};
                })
                .onFailure(emitter::fail)
                .onSuccess(result -> emitter.complete());
    }
}
