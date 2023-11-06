package com.getlipa.eventstore.core.actor.messaging;

import com.getlipa.eventstore.actors.Actors;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.proto.Payload;
import com.getlipa.eventstore.core.proto.ProtoEncodable;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.google.protobuf.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Builder(setterPrefix = "with", builderMethodName = "create", buildMethodName = "withPayload")
@ToString(onlyExplicitlyIncluded = true)
public class Msg<T extends Message> extends ProtoEncodable<Actors.Msg> implements AnyMsg {

    public static final String CODEC = "proto-msg";

    @ToString.Include
    private final UUID id;

    private final UUID causationId;

    private final UUID correlationId;

    private final Payload<T> payload;

    public static <T extends Message> Msg<T> withPayload(T data) {
        return init().withPayload(data);
    }

    public static <T extends Message> Msg<T> withPayload(ProtoEncodable<T> data) {
        return init().withPayload(data);
    }

    private static Msg.MsgBuilder<Message> init() {
        return Msg.create()
                .withId(UUID.randomUUID())
                .withCausationId(UUID.randomUUID())
                .withCorrelationId(UUID.randomUUID());
    }

    @Override
    protected Actors.Msg encodeToProto() {
        return Actors.Msg.newBuilder()
                .setId(ProtoUtil.convert(getId()))
                .setCausationId(ProtoUtil.convert(getCausationId()))
                .setCorrelationId(ProtoUtil.convert(getCorrelationId()))
                .setPayload(ProtoUtil.convert(getPayload()))
                .build();
    }

    public static class MsgBuilder<T extends Message> extends ProtoEncodable.Builder<Msg<T>, Actors.Msg> {

        public <P extends Message> Msg<P> withPayload(P payload) {
            return withPayload(Payload.create(payload));
        }

        public <P extends Message> Msg<P> withPayload(ProtoEncodable<P> payload) {
            return withPayload(Payload.create(payload));
        }

        public <P extends Message> Msg<P> withPayload(Payload<P> payload) {
            return new Msg<>(
                    id,
                    causationId,
                    correlationId,
                    payload
            );
        }

        @Override
        protected Msg<T> decodeFromProto(Actors.Msg command) {
            return withCausationId(ProtoUtil.toUUID(Event.EVENT_CAUSATION_ID_NAMESPACE, command.getCausationId()))
                    .withCorrelationId(ProtoUtil.toUUID(Event.EVENT_CORRELATION_ID_NAMESPACE, command.getCorrelationId()))
                    .withPayload(Payload.create(command.getPayload()));
        }
    }
}
