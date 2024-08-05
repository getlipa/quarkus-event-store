package com.getlipa.eventstore.aggregate.context;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.identifier.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NamedContext extends Context {

    private final String name;

    @Override
    public Id extractAggregateId(AnyEvent event) {
        return event.getLogId();
    }

    @Override
    public Selector createSelector() {
        return Events.byContext(name);
    }

    @Override
    public ByLogSelector createSelector(Id aggregateId) {
        return Events.byLog(name, aggregateId);
    }

    @Override
    public String toString() {
        return name;
    }
}
