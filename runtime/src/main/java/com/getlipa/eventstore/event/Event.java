package com.getlipa.eventstore.event;

import com.getlipa.eventstore.event.payload.Payload;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.subscriptions.Projections;
import com.google.protobuf.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@EqualsAndHashCode(callSuper = true)
public class Event<T extends Message> extends AbstractEvent<T> implements AnyEvent {

    private final long position;

    private final long logIndex;

    private final String logContext;

    private final Id logId;

    public Event(
            final Id id,
            final Id causationId,
            final Id correlationId,
            final OffsetDateTime createdAt,
            final Payload<T> payload,
            final long position,
            final long logIndex,
            final String logContext,
            final Id logId
    ) {
        super(id, causationId, correlationId, createdAt, payload);
        this.position = position;
        this.logIndex = logIndex;
        this.logContext = logContext;
        this.logId = logId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static AnyEvent from(final Projections.Event event) {
        return builder()
                .withId(Id.from(event.getId()))
                .withPosition(event.getPosition())
                .withLogIndex(event.getLogIndex())
                .withLogDomain(event.getLogDomain())
                .withLogId(Id.from(event.getLogId()))
                .withCausationId(Id.from(event.getCausationId()))
                .withCorrelationId(Id.from(event.getCorrelationId()))
                .withCreatedAt(OffsetDateTime.ofInstant(Instant.ofEpochSecond(
                        event.getCreatedAt().getSeconds(),
                        event.getCreatedAt().getNanos()), ZoneId.of("UTC"))
                )
                .withPayload(Payload.encoded(event.getPayload()));
    }

    public static Builder from(final EventMetadata event) {
        return builder()
                .withId(event.getId())
                .withPosition(event.getPosition())
                .withLogIndex(event.getLogIndex())
                .withLogDomain(event.getLogContext())
                .withLogId(event.getLogId())
                .withCausationId(event.getCausationId())
                .withCorrelationId(event.getCorrelationId())
                .withCreatedAt(event.getCreatedAt());
    }

    public static EphemeralEvent.Builder withId(final Id id) {
        return init().withId(id);
    }

    public static EphemeralEvent.Builder withCausationId(final Id causationId) {
        return init().withCausationId(causationId);
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
        final var deterministicId = Id.derive(reason, getId());
        return Event.withCausationId(getId())
                .withId(deterministicId);
    }

    @Override
    public Projections.Event toProto() {
        return Projections.Event.newBuilder()
                .setId(getId().toByteString())
                .setPosition(getPosition())
                .setLogIndex(getLogIndex())
                .setLogDomain(getLogContext())
                .setLogId(getLogId().toByteString())
                .setCreatedAt(ProtoUtil.convert(getCreatedAt()))
                .setCausationId(getCausationId().toByteString())
                .setCorrelationId(getCorrelationId().toByteString())
                .setPayload(getPayload().toProto())
                .build();
    }

    @Override
    public String toString() {
        return String.format(
                "%s{%s %s-%s@%s @%s}",
                getPayload().toString(),
                id,
                logContext,
                logId,
                logIndex,
                position
        );
    }

    public T get() {
        return getPayload().get();
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder extends AbstractEvent.Builder<Builder> {

        private long withPosition;

        private long withLogIndex;

        private String withLogDomain;

        private Id withLogId;

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
