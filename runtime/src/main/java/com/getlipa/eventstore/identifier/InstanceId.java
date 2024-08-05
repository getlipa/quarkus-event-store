package com.getlipa.eventstore.identifier;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Singleton
public class InstanceId {

    private final Id id = Id.random();

    @PostConstruct
    void onStartup() {
        log.info("Started with instance id: {}", id);
    }
}
