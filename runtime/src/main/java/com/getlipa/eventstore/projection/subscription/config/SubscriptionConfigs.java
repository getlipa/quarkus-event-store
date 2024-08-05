package com.getlipa.eventstore.projection.subscription.config;

import io.smallrye.config.*;

import java.util.Map;

@ConfigMapping(prefix = "quarkus.eventstore.projection")
public interface SubscriptionConfigs {

    /**
     * Holds all subscription configs by projection name
     * @return subscription configs
     */
    @WithParentName
    @WithDefaults
    Map<String, SubscriptionConfig> all();
}
