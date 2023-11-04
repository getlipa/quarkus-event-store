package com.getlipa.eventstore.core.proto;

import com.getlipa.eventstore.common.Common;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class Payload<T extends Message> implements AnyPayload {

    public static final String PAYLOAD_TYPE_NAMESPACE = "$payload-type";

    private final Supplier<T> payloadSupplier;

    private T payload;

    public static <T extends Message> Payload<T> create(Common.Payload payload) {
        return create((Supplier<T>) () -> PayloadParser.instance().parse(payload));
    }

    public static <T extends Message> Payload<T> create(T payload) {
        return create((Supplier<T>) () -> payload);
    }

    public static <T extends Message> Payload<T> create(Supplier<T> payloadSupplier) {
        return new Payload<>(payloadSupplier);
    }

    public static <T extends Message> Payload<T> create(UUID type, byte[] payload) {
        return new Payload<>(() -> PayloadParser.instance().parse(type, payload));
    }

    public T get() {
        if (payload == null) {
            payload = payloadSupplier.get();
        }
        return payload;
    }

    public <P extends T> P get(Class<P> type) {
        if (payload == null) {
            payload = payloadSupplier.get();
        }
        if (type.isInstance(payload)) {
            return (P) payload;
        }
        return null;
    }

    public Class<? extends Message> getType() {
        final var payload = get();
        if (payload == null) {
            return Message.class;
        }
        return payload.getClass();
    }

    public UUID getTypeId() {
        return ProtoUtil.toUUID(get().getDescriptorForType());
    }
}
