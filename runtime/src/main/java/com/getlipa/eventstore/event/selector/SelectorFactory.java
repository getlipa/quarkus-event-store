package com.getlipa.eventstore.event.selector;


import com.getlipa.eventstore.identifier.Id;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SelectorFactory {

    WITH_LOG_ID(ByLogIdSelector::new, ByLogIdSelector::new),
    WITH_LOG_DOMAIN(ByContextSelector::new, null),
    WITH_CORRELATION_ID(ByCorrelationIdSelector::new, ByCorrelationIdSelector::new),
    WITH_CAUSATION_ID(ByCausationIdSelector::new, ByCausationIdSelector::new),
    WITH_TYPE(ByTypeSelector::new, null);


    private final Function<String, Selector> deprecatedEventMatcherFactory;

    private final Function<Id, Selector> eventMatcherFactory;

    @Deprecated
    public Selector create(String parameter) {
        return deprecatedEventMatcherFactory.apply(parameter);
    }

    public Selector create(Id id) {
        if (eventMatcherFactory == null) {
            throw new UnsupportedOperationException("EventMatcher creation by Id is not supported: " + this);
        }
        return eventMatcherFactory.apply(id);
    }
}
