package com.getlipa.eventstore.core.event;

import com.getlipa.eventstore.core.proto.Payload;
import com.google.protobuf.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Getter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class Event<T extends Message> extends AbstractEvent<T> implements AnyEvent {

    private final long position;

    private final long seriesIndex;

    private final UUID seriesType;

    private final UUID seriesId;

    @Builder
    public Event(
            UUID id,
            UUID causationId,
            UUID correlationId,
            OffsetDateTime createdAt,
            Supplier<T> payload,
            long position,
            long seriesIndex,
            UUID seriesType,
            UUID seriesId
    ) {
        super(id, causationId, correlationId, createdAt, Payload.create(payload));
        this.position = position;
        this.seriesIndex = seriesIndex;
        this.seriesType = seriesType;
        this.seriesId = seriesId;
    }

    public static <T extends Message> EventBuilder<T> from(EventMetadata event) {
        return Event.<T>builder()
                .id(event.getId())
                .position(event.getPosition())
                .seriesIndex(event.getSeriesIndex())
                .seriesType(event.getSeriesType())
                .seriesId(event.getSeriesId())
                .causationId(event.getCausationId())
                .correlationId(event.getCorrelationId())
                .createdAt(event.getCreatedAt());
    }

    public static EphemeralEvent.EphemeralEventBuilder<Message> create() {
        return init();
    }

    public static EphemeralEvent.EphemeralEventBuilder<Message> withId(UUID id) {
        return init().withId(id);
    }

    public static EphemeralEvent.EphemeralEventBuilder<Message> withCausationId(UUID causationId) {
        return init().withCausationId(causationId);
    }

    public static EphemeralEvent.EphemeralEventBuilder<Message> withCorrelationId(UUID correlationId) {
        return init().withCorrelationId(correlationId);
    }

    public static EphemeralEvent.EphemeralEventBuilder<Message> withCreatedAt(OffsetDateTime createdAt) {
        return init().withCreatedAt(createdAt);
    }

    public static <T extends Message> EphemeralEvent<T> withPayload(T data) {
        return init().withPayload(data);
    }

    private static EphemeralEvent.EphemeralEventBuilder<Message> init() {
        return EphemeralEvent.create()
                .withId(UUID.randomUUID())
                .withCausationId(UUID.randomUUID())
                .withCorrelationId(UUID.randomUUID())
                .withCreatedAt(OffsetDateTime.now());
    }

    public EphemeralEvent.EphemeralEventBuilder<Message> causeOther(String reason) {
        final var deterministicId = UUID.nameUUIDFromBytes(
                String.format("%s-%s", getId(), reason)
                        .getBytes(StandardCharsets.UTF_8)
        );
        return Event.withCausationId(getId())
                .withId(deterministicId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Message> Event<T> on(Class<P> type, AnyEvent.Handler<P> handler) {
        if (type.isInstance(payload())) {
            handler.handle((Event<P>) this);
        }
        return this;
    }
}
