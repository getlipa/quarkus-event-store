package com.getlipa.eventstore.core.proto;

import com.getlipa.eventstore.common.Common;
import com.getlipa.eventstore.core.UuidGenerator;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Timestamp;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ProtoUtil {

    private static final UuidGenerator uuidGenerator = UuidGenerator.INSTANCE;
    public static Common.Payload convert(AnyPayload payload) {
        return Common.Payload.newBuilder()
                .setType(toBytes(payload))
                .setData(payload.get().toByteString())
                .build();
    }

    public static Common.Payload convert(Payload<?> payload) {
        return Common.Payload.newBuilder()
                .setType(convert(payload.getTypeId()))
                .setData(payload.get().toByteString())
                .build();
    }

    public static Timestamp convert(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static ByteString convert(UUID uuid) {
        return ByteString.copyFrom(toBytes(uuid));
    }


    public static UUID toUUID(final String namespace, final String data) {
        return uuidGenerator.generate(namespace, data);
    }

    public static UUID toUUID(final Descriptors.Descriptor descriptors) {
        return toUUID(Payload.PAYLOAD_TYPE_NAMESPACE, descriptors.getFullName());
    }

    public static ByteString toBytes(AnyPayload payload) {
        return convert(ProtoUtil.toUUID(payload.get().getDescriptorForType()));
    }

    public static UUID toUUID(final byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    public static UUID toUUID(ByteString bytes) {
        return toUUID(bytes.toByteArray());
    }

    public static Timestamp convert(OffsetDateTime createdAt) {
        return convert(createdAt.toInstant());
    }
}
