package com.getlipa.eventstore.core.util;

import com.getlipa.eventstore.core.UuidGenerator;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@Getter
@Singleton
public class InstanceId {

    private final UUID uuid = UUID.randomUUID();

    @PostConstruct
    void onStartup() {
        log.info("Started with instance id: {}", uuid);
    }

    public UUID getUuid(String namespace) {
        return UuidGenerator.INSTANCE.generate(namespace, null); // FIXME
    }
}
