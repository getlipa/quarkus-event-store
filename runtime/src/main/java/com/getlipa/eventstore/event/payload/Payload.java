package com.getlipa.eventstore.event.payload;

import com.getlipa.eventstore.common.Common;
import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Message;

public interface Payload<T extends Message> extends AnyPayload {

    String PAYLOAD_TYPE_NAMESPACE = "$payload-type";

    static <T extends Message> Payload<T> encoded(Id typeId, byte[] payload) {
        return new SerializedPayload<>(
            typeId,
            payload
        );
    }

    static <T extends Message> Payload<T> encoded(Common.Payload payload) {
        return encoded(Id.from(payload.getType()), payload.getData().toByteArray());
    }

    static Payload<Message> empty() {
        return new DeserializedPayload<>(null, null); // FIXME
    }

    static <T extends Message> Payload<T> create(T payload) {
        return new DeserializedPayload<>(
                PayloadDeserializer.typeId(payload.getDescriptorForType()),
                payload
        );
    }

    T get();

    Id getTypeId();

    Common.Payload toProto();
}
