package com.getlipa.eventstore.core.proto;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ProtoUtil {

    public static UUID toUUID(UUID uuid) {
        return uuid;
    }

    public static UUID toUUID(String data) {
        return UUID.nameUUIDFromBytes(data.getBytes(StandardCharsets.UTF_8));
    }
}
