package com.getlipa.eventstore.core.actor.messaging;

import com.getlipa.eventstore.core.proto.Payload;
import com.google.protobuf.Message;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;
import java.util.function.Supplier;

@Getter
public class Result<T extends Message> {

    private final UUID id;

    private final UUID causationId;

    private final UUID correlationId;

    private final Payload<T> payload;

    @Builder(setterPrefix = "with", builderMethodName = "create", buildMethodName = "withPayload")
    protected Result(
            UUID id,
            UUID causationId,
            UUID correlationId,
            Supplier<T> payload
    ) {
        this.id = id;
        this.causationId = causationId;
        this.correlationId = correlationId;
        this.payload = Payload.create(payload);
    }

    public static ResultBuilder<Message> createFor(Command<?> command) {
        return create()
                .withCausationId(command.getId())
                .withCorrelationId(command.getCorrelationId());
    }

    public static class ResultBuilder<T extends Message> {
        public <P extends Message> Result<P> withLazyPayload(Supplier<P> payloadSupplier) {
            return new Result<>(
                    id,
                    causationId,
                    correlationId,
                    payloadSupplier
            );
        }
        public <P extends Message> Result<P> withPayload(P payload) {
            return withLazyPayload(() -> payload);
        }
    }
}
