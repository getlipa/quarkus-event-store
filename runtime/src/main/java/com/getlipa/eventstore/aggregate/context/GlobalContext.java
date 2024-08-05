package com.getlipa.eventstore.aggregate.context;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.identifier.Id;

public class GlobalContext extends Context {

    @Override
    public Id extractAggregateId(AnyEvent event) {
        return event.getCorrelationId();
    }

    @Override
    public Selector createSelector() {
        return Events.all();
    }

    @Override
    public Selector createSelector(Id aggregateId) {
        return Events.byCorrelationId(aggregateId);
    }

    @Override
    public String toString() {
        return "$global";
    }
}
