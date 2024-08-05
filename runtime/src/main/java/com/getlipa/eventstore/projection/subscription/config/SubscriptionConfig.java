package com.getlipa.eventstore.projection.subscription.config;

import io.smallrye.config.WithDefault;


public interface SubscriptionConfig {

    /**
     * Defines subscription config
     * @return Subscription config
     */
    Config subscription();

    interface Config {

        /**
         * Defines whether a subscription should be registered at startup
         * @return true if subscription should be registered
         */
        @WithDefault(value = "false")
        boolean enabled();

        /**
         * Defines the checkpointing interval in milliseconds
         * @return checkpointing interval in milliseconds
         */
        @WithDefault(value = "60000")
        int checkpointIntervalMs();
    }
}
