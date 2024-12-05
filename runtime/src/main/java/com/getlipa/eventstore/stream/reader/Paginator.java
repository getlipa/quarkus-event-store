package com.getlipa.eventstore.stream.reader;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.persistence.EventPersistence;
import com.getlipa.eventstore.stream.reader.cursor.Cursor;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RequiredArgsConstructor
public class Paginator {

    private final EventPersistence eventPersistence;

    private final Selector selector;

    private final ReadOptions readOptions;

    public static Paginator create(
            final EventPersistence eventPersistence,
            final Selector selector,
            final ReadOptions readOptions
    ) {
        return new Paginator(
                eventPersistence,
                selector,
                readOptions
        );
    }

    public Uni<Long> forEach(AnyEvent.Handler<?> eventHandler) {
        return readAll()
                .onItem().transformToUni(event -> Uni.createFrom().completionStage(
                        eventHandler.handle(event).toCompletionStage()
                ))
                .concatenate()
                .collect().in(AtomicLong::new, (eventsCount, result) -> eventsCount.incrementAndGet())
                .map(AtomicLong::longValue);
    }

    public Multi<AnyEvent> readAll() {
        return Multi.createBy().repeating()
                .uni(
                        () -> new AtomicReference<Page>(),
                        currentPage -> nextPage(currentPage.get()).invoke(currentPage::set)
                )
                .until(Page::isEmpty)
                .onItem().transformToMultiAndConcatenate(Page::events);

    }

    Uni<Page> nextPage(final Page currentPage) {
        if (currentPage == null) {
            return Uni.createFrom().completionStage(eventPersistence.read(selector, ReadOptions.from(readOptions)
                            .limit(Integer.min(readOptions.limit(), readOptions.pageSize()))
                            .build())
                    .map(iterator -> Page.first(readOptions, iterator))
                    .toCompletionStage());
        }
        if (currentPage.remainingTotalLimit <= 0) {
            return Uni.createFrom().item(Page.empty());
        }
        return Uni.createFrom().completionStage(eventPersistence.read(
                        selector,
                        ReadOptions.from(readOptions)
                                .from(currentPage.cursor())
                                .limit(Integer.min((int) currentPage.remainingTotalLimit, readOptions.pageSize()) + 1)
                                .build()
                )
                .map(currentPage::createNext)
                .toCompletionStage());
    }

    @RequiredArgsConstructor
    static class Page {

        final long remainingTotalLimit;

        final LinkedList<AnyEvent> events;

        static Page first(final ReadOptions readOptions, final Iterator<AnyEvent> iterator) {
            final var events = list(iterator);
            if (events.isEmpty()) {
                return Page.empty();
            }
            return new Page(readOptions.limit() - events.size(), events);
        }

        static Page empty() {
            return new Page(Long.MAX_VALUE, new LinkedList<>());
        }

        static LinkedList<AnyEvent> list(final Iterator<AnyEvent> iterator) {
            final var events = new LinkedList<AnyEvent>();
            iterator.forEachRemaining(events::add);
            return events;
        }

        Page createNext(final Iterator<AnyEvent> iterator) {
            final var events = list(iterator);
            if (events.isEmpty() || events.removeFirst().getPosition() != this.events.getLast().getPosition()) {
                throw new IllegalStateException("Stream was truncated during read."); // FIXME
            }
            return new Page(remainingTotalLimit - events.size(), events);
        }

        Cursor cursor() {
            if (isEmpty()) {
                return Cursor.streamEnd();
            }
            return Cursor.position(events.getLast().getPosition());
        }

        boolean isEmpty() {
            return events.isEmpty();
        }

        Multi<AnyEvent> events() {
            return Multi.createFrom().iterable(events);
        }
    }
}
