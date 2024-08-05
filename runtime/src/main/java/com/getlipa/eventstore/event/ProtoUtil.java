package com.getlipa.eventstore.event;

import com.google.protobuf.Timestamp;

import java.time.Instant;
import java.time.OffsetDateTime;

public class ProtoUtil {

    public static Timestamp convert(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    public static Timestamp convert(OffsetDateTime createdAt) {
        return convert(createdAt.toInstant());
    }
}
