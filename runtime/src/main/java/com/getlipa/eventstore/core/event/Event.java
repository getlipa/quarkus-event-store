package com.getlipa.eventstore.core.event;

import com.getlipa.eventstore.core.UuidGenerator;
import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.core.proto.ProtoEncodable;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.subscriptions.Subscriptions;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Getter
@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class Event<T extends Message> extends AbstractEvent<T> implements AnyEvent {


    public static final String EVENT_ID_NAMESPACE = "$event-id";
    public static final String EVENT_TYPE_NAMESPACE = Payload.PAYLOAD_TYPE_NAMESPACE;
    public static final String EVENT_SERIES_TYPE_NAMESPACE = "$event-series-type";
    public static final String EVENT_SERIES_ID_NAMESPACE = "$event-series-id";
    public static final String EVENT_CORRELATION_ID_NAMESPACE = "$event-correlation-id";
    public static final String EVENT_CAUSATION_ID_NAMESPACE = "$event-causation-id";


    private final long position;

    private final long seriesIndex;

    private final UUID seriesType;

    private final UUID seriesId;
    private static final UuidGenerator uuidGenerator = UuidGenerator.INSTANCE;

    @lombok.Builder(setterPrefix = "with", buildMethodName = "withPayload")
    public Event(
            UUID id,
            UUID causationId,
            UUID correlationId,
            OffsetDateTime createdAt,
            Payload<T> payload,
            long position,
            long seriesIndex,
            UUID seriesType,
            UUID seriesId
    ) {
        super(id, causationId, correlationId, createdAt, payload);
        this.position = position;
        this.seriesIndex = seriesIndex;
        this.seriesType = seriesType;
        this.seriesId = seriesId;
    }

    public static AnyEvent from(Payload<Subscriptions.Event> eventPayload) {
        return from(eventPayload.get());
    }

    public static AnyEvent from(Subscriptions.Event event) {
        return Event.builder().decodeFromProto(event);
    }

    public static <T extends Message> EventBuilder<T> from(EventMetadata event) {
        return Event.<T>builder()
                .withId(event.getId())
                .withPosition(event.getPosition())
                .withSeriesIndex(event.getSeriesIndex())
                .withSeriesType(event.getSeriesType())
                .withSeriesId(event.getSeriesId())
                .withCausationId(event.getCausationId())
                .withCorrelationId(event.getCorrelationId())
                .withCreatedAt(event.getCreatedAt());
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
        final var deterministicId = uuidGenerator.generate(Event.EVENT_ID_NAMESPACE, String.format("%s-%s", getId(), reason));
        return Event.withCausationId(getId())
                .withId(deterministicId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Message> Event<T> on(Class<P> type, AnyEvent.Handler<P> handler) {
        if (type.isInstance(getPayload().get())) {
            handler.handle((Event<P>) this);
        }
        return this;
    }

    @Override
    protected Subscriptions.Event encodeToProto() {
        return Subscriptions.Event.newBuilder()
                .setId(ProtoUtil.convert(getId()))
                .setPosition(getPosition())
                .setSeriesIndex(getSeriesIndex())
                .setSeriesType(ProtoUtil.convert(getSeriesType()))
                .setSeriesId(ProtoUtil.convert(getSeriesId()))
                .setCreatedAt(ProtoUtil.convert(getCreatedAt()))
                .setCausationId(ProtoUtil.convert(getCausationId()))
                .setCorrelationId(ProtoUtil.convert(getCorrelationId()))
                .setPayload(ProtoUtil.convert(getPayload()))
                .build();
    }

    public static class EventBuilder<T extends Message> extends ProtoEncodable.Builder<Event<T>, Subscriptions.Event> {

        @Override
        protected Event<T> decodeFromProto(Subscriptions.Event event) {
            return Event.builder()
                    .withId(ProtoUtil.toUUID(event.getId()))
                    .withPosition(event.getPosition())
                    .withSeriesIndex(event.getSeriesIndex())
                    .withSeriesType(ProtoUtil.toUUID(event.getSeriesType()))
                    .withSeriesId(ProtoUtil.toUUID(event.getSeriesId()))
                    .withCausationId(ProtoUtil.toUUID(event.getCausationId()))
                    .withCorrelationId(ProtoUtil.toUUID(event.getCorrelationId()))
                    .withCreatedAt(OffsetDateTime.ofInstant(Instant.ofEpochSecond(
                            event.getCreatedAt().getSeconds(),
                            event.getCreatedAt().getNanos()), ZoneId.of("UTC"))
                    )
                    .withPayload(Payload.create(event.getPayload()));
        }

        public <P extends Message> Event<P> withPayload(P payload) {
            return withPayload(Payload.create(payload));
        }

        public <P extends Message> Event<P> withPayload(Payload<P> payload) {
            return new Event<>(
                    id,
                    causationId,
                    correlationId,
                    createdAt,
                    payload,
                    position,
                    seriesIndex,
                    seriesType,
                    seriesId
            );
        }
    }
}
