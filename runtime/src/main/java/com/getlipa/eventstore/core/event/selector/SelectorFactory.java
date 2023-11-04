package com.getlipa.eventstore.core.event.selector;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SelectorFactory {

    WITH_LOG_ID(ByLogIdSelector::new),
    WITH_LOG_DOMAIN(ByLogDomainSelector::new),
    WITH_CORRELATION_ID(ByCorrelationIdSelector::new),
    WITH_CAUSATION_ID(ByCausationIdSelector::new),

    WITH_TYPE(ByTypeSelector::new);


    private final Function<String, Selector> eventMatcherFactory;

    public Selector create(String parameter) {
        return eventMatcherFactory.apply(parameter);
    }
}
