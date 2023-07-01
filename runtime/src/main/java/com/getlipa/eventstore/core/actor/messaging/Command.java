package com.getlipa.eventstore.core.actor.messaging;

import com.getlipa.eventstore.actors.Actors;
import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.core.proto.PayloadParser;
import com.google.protobuf.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.With;

import java.util.UUID;
import java.util.function.Supplier;

@Getter
@Builder(setterPrefix = "with", builderMethodName = "create", buildMethodName = "withPayload")
@ToString(onlyExplicitlyIncluded = true)
public class Command<T extends Message> {

    @ToString.Include
    private final UUID id;

    private final UUID causationId;

    private final UUID correlationId;

    private final Payload<T> payload;

    @With
    private final UUID origin;

    public static <T extends Message> Command<T> withPayload(T data) {
        return init().withPayload(data);
    }

    private static Command.CommandBuilder<Message> init() {
        return Command.create()
                .withId(UUID.randomUUID())
                .withCausationId(UUID.randomUUID())
                .withCorrelationId(UUID.randomUUID());
    }

    public EphemeralEvent.EphemeralEventBuilder<Message> createEvent() {
        return Event.create()
                .withCausationId(getId())
                .withCorrelationId(getCorrelationId());
    }

    public static class CommandBuilder<T extends Message> {

        public <P extends Message> Command<P> withLazyPayload(Supplier<P> payloadSupplier) {
            return withPayload(Payload.create(payloadSupplier));
        }

        public <P extends Message> Command<P> withPayload(P payloadSupplier) {
            return withPayload(Payload.create(payloadSupplier));
        }

        public <P extends Message> Command<P> withPayload(Payload<P> payload) {
            return new Command<>(
                    id,
                    causationId,
                    correlationId,
                    payload,
                    origin
            );
        }
    }

    public static Command<? extends Message> from(Actors.Command command, PayloadParser payloadParser) {
        final var payload = payloadParser.parse(command.getPayload());
        return Command.create()
                // TODO: set metadata according to proto data
                .withPayload(payload);
    }
}
