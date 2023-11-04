package com.getlipa.eventstore.core.projection.projector;

import com.getlipa.eventstore.core.event.AnyEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DispatchStrategy {

    BY_LOG_ID(AnyEvent::getLogId),
    BY_LOG_DOMAIN(AnyEvent::getLogDomain);

    private final Function<AnyEvent, Object> aggregationIdGetter;

    public static DispatchStrategy defaultType() {
        return BY_LOG_ID;
    }

    public String determineTargetId(AnyEvent event) {
        return aggregationIdGetter.apply(event).toString();
    }

}
