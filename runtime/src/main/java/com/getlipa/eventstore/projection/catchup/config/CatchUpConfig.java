package com.getlipa.eventstore.projection.catchup.config;

import io.smallrye.config.WithDefault;


public interface CatchUpConfig {

    /**
     * Defines catch-up config
     * @return Catch-up config
     */
    Config catchUp();

    interface Config {

        /**
         * Defines whether a catch-up job should be started at startup
         * @return true if a catch-up job should be started at startup
         */
        @WithDefault(value = "false")
        boolean atStartup();

        /**
         * Defines the checkpointing interval in milliseconds
         * @return checkpointing interval in milliseconds
         */
        @WithDefault(value = "60000")
        int checkpointIntervalMs();
    }
}
