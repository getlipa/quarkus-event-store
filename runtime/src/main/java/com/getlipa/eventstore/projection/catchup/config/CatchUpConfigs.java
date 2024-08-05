package com.getlipa.eventstore.projection.catchup.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;

import java.util.Map;

@ConfigMapping(prefix = "quarkus.eventstore.projection")
public interface CatchUpConfigs {

    /**
     * Holds all catch-up configs by projection name
     * @return catch-up configs
     */
    @WithParentName
    @WithDefaults
    Map<String, CatchUpConfig> all();
}
