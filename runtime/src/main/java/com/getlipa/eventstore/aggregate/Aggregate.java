package com.getlipa.eventstore.aggregate;

import com.getlipa.eventstore.event.EventMetadata;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.identifier.Id;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class Aggregate<T> {

    private static final long EMPTY_STREAM_REVISION = -1;

    @Getter
    private final Id id;

    protected final Hydrator<T> hydrator;

    protected final EventTipHolder<T> eventTipHolder;

    public static <T> Aggregate<T> create(
            final Id id,
            final Hydrator<T> hydrator,
            final EventTipHolder<T> eventTipHolder

    ) {
        return new Aggregate<>(id, hydrator, eventTipHolder);
    }

    public Future<T> get() {
        return get(false);
    }

    public Future<T> get(final boolean refresh) {
        if (refresh) {
            return hydrator.refreshed()
                    .onFailure(error -> log.warn("Unable to refresh target: {}", error.getMessage()));
        }
        return hydrator.initialized()
                .onFailure(error -> log.warn("Unable to initialize target: {}", error.getMessage()));
    }

    public long getRevision() {
        return Optional.ofNullable(eventTipHolder.getEvent())
                .map(this::extractRevision)
                .orElse(EMPTY_STREAM_REVISION);
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", hydrator.getType().getSimpleName(), id);
    }

    protected long extractRevision(EventMetadata eventMetadata) {
        return eventMetadata.getPosition();
    }

}
