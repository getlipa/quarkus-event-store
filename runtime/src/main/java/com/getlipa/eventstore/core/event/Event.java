package com.getlipa.eventstore.core.event;

import com.getlipa.eventstore.core.UuidGenerator;
import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.subscriptions.Projections;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Getter
//@ToString(onlyExplicitlyIncluded = true, callSuper = true)
public class Event<T extends Message> extends AbstractEvent<T> implements AnyEvent {


    public static final String EVENT_ID_NAMESPACE = "$event-id";
    public static final String EVENT_TYPE_NAMESPACE = Payload.PAYLOAD_TYPE_NAMESPACE;
    public static final String EVENT_LOG_DOMAIN_NAMESPACE = "$event-series-type";
    public static final String EVENT_LOG_ID_NAMESPACE = "$event-series-id";
    public static final String EVENT_CORRELATION_ID_NAMESPACE = "$event-correlation-id";
    public static final String EVENT_CAUSATION_ID_NAMESPACE = "$event-causation-id";


    private final long position;


    /*
    FIXME: Alternative names: !!!!!!!!!!
    * EventLog / Log / Log Book / Journal -> indicate completenes
    * Compendium
     */
    private final long logIndex;

    private final String logDomain;

    private final UUID logId;

    private static final UuidGenerator uuidGenerator = UuidGenerator.INSTANCE;

    public Event(
            final UUID id,
            final UUID causationId,
            final UUID correlationId,
            final OffsetDateTime createdAt,
            final Payload<T> payload,
            final long position,
            final long logIndex,
            final String logDomain,
            final UUID logId
    ) {
        super(id, causationId, correlationId, createdAt, payload);
        this.position = position;
        this.logIndex = logIndex;
        this.logDomain = logDomain;
        this.logId = logId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static AnyEvent from(final Projections.Event event) {
        return builder()
                .withId(ProtoUtil.toUUID(event.getId()))
                .withPosition(event.getPosition())
                .withLogIndex(event.getLogIndex())
                .withLogDomain(event.getLogDomain())
                .withLogId(ProtoUtil.toUUID(event.getLogId()))
                .withCausationId(ProtoUtil.toUUID(event.getCausationId()))
                .withCorrelationId(ProtoUtil.toUUID(event.getCorrelationId()))
                .withCreatedAt(OffsetDateTime.ofInstant(Instant.ofEpochSecond(
                        event.getCreatedAt().getSeconds(),
                        event.getCreatedAt().getNanos()), ZoneId.of("UTC"))
                )
                .withPayload(Payload.create(event.getPayload()));
    }

    public static Builder from(final EventMetadata event) {
        return builder()
                .withId(event.getId())
                .withPosition(event.getPosition())
                .withLogIndex(event.getLogIndex())
                .withLogDomain(event.getLogDomain())
                .withLogId(event.getLogId())
                .withCausationId(event.getCausationId())
                .withCorrelationId(event.getCorrelationId())
                .withCreatedAt(event.getCreatedAt());
    }

    public static EphemeralEvent.Builder withId(final UUID id) {
        return init().withId(id);
    }

    public static EphemeralEvent.Builder withCausationId(final UUID causationId) {
        return init().withCausationId(causationId);
    }

    public static EphemeralEvent.Builder withCorrelationId(final UUID correlationId) {
        return init().withCorrelationId(correlationId);
    }

    public static EphemeralEvent.Builder withCreatedAt(final OffsetDateTime createdAt) {
        return init().withCreatedAt(createdAt);
    }

    public static <T extends Message> EphemeralEvent<T> withPayload(final T data) {
        return init().withPayload(data);
    }

    private static EphemeralEvent.Builder init() {
        return EphemeralEvent.create();
    }

    public EphemeralEvent.Builder causeOther(final String reason) {
        final var deterministicId = uuidGenerator.generate(Event.EVENT_ID_NAMESPACE, String.format("%s-%s", getId(), reason));
        return Event.withCausationId(getId())
                .withId(deterministicId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends Message> Event<T> on(final Class<P> type, final Handler<P> handler) {
        if (type.isInstance(getPayload().get())) {
            handler.handle((Event<P>) this); // TODO: future?
        }
        return this;
    }

    @Override
    public Projections.Event toProto() {
        return Projections.Event.newBuilder()
                .setId(ProtoUtil.convert(getId()))
                .setPosition(getPosition())
                .setLogIndex(getLogIndex())
                .setLogDomain(getLogDomain())
                .setLogId(ProtoUtil.convert(getLogId()))
                .setCreatedAt(ProtoUtil.convert(getCreatedAt()))
                .setCausationId(ProtoUtil.convert(getCausationId()))
                .setCorrelationId(ProtoUtil.convert(getCorrelationId()))
                .setPayload(ProtoUtil.convert(getPayload()))
                .build();
    }

    @Override
    public String toString() {
        return String.format(
                "%s(%s %s-%s@%s @%s)",
                getPayload().getType().getSimpleName(),
                id,
                logDomain,
                logId,
                logIndex,
                position
        );
    }

    public interface Handler<T extends Message> {

        Future<Void> handle(Event<T> event);
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder extends AbstractEvent.Builder<Builder> {

        private long withPosition;

        private long withLogIndex;

        private String withLogDomain;

        private UUID withLogId;

        public <T extends Message> Event<T> withPayload(final T payload) {
            return withPayload(Payload.create(payload));
        }

        public <T extends Message> Event<T> withPayload(final Payload<T> payload) {
            return new Event<>(
                    withId,
                    withCausationId,
                    withCorrelationId,
                    withCreatedAt,
                    payload,
                    withPosition,
                    withLogIndex,
                    withLogDomain,
                    withLogId
            );
        }
    }
}
