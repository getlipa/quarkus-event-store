package com.getlipa.eventstore.event.payload;

import com.getlipa.eventstore.common.Common;
import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SerializedPayload<T extends Message> extends AbstractPayload<T> {

    @Getter
    private final Id typeId;

    private final byte[] data;

    private Payload<T> payload;

    public T get() {
        return payload().get();
    }

    @Override
    public Common.Payload toProto() {
        return payload().toProto();
    }

    @Override
    public String toString() {
        if (payload != null) {
            return payload.toString();
        }
        return String.format("Serialized.%s[%d]", typeId, data.length);
    }

    Payload<T> payload() {
        if (payload == null) {
            payload = Payload.create(PayloadDeserializer.deserialize(typeId, data));
        }
        return payload;
    }
}
