package com.getlipa.eventstore.projection.subscription;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubscriptionConfig {

    private final boolean subscribe = true; // FIXME

    private final long checkpointIntervalMs = 1000L;

}
