package com.getlipa.eventstore.event.payload;

import com.getlipa.eventstore.common.Common;
import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Message;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeserializedPayload<T extends Message> extends AbstractPayload<T> {

    @Getter
    private final Id typeId;

    private final T payload;

    public T get() {
        return payload;
    }

    @Override
    public Common.Payload toProto() {
        return Common.Payload.newBuilder()
                .setType(typeId.toByteString())
                .setData(payload.toByteString())
                .build();
    }

    @Override
    public String toString() {
        return String.format("%s", payload.getClass().getSimpleName());
    }

}
