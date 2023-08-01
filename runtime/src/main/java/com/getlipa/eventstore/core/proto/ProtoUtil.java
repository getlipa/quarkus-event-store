package com.getlipa.eventstore.core.proto;

import com.getlipa.eventstore.common.Common;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ProtoUtil {

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

    public static UUID toUUID(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();
        return new UUID(high, low);
    }

    public static UUID toUUID(UUID uuid) {
        return uuid;
    }

    public static UUID toUUID(String data) {
        return UUID.nameUUIDFromBytes(data.getBytes(StandardCharsets.UTF_8));
    }

    public static UUID toUUID(ByteString bytes) {
        return toUUID(bytes.toByteArray());
    }

    public static Timestamp convert(OffsetDateTime createdAt) {
        return convert(createdAt.toInstant());
    }
}