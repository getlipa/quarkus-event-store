package com.getlipa.eventstore.aggregate.context;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.selector.Selector;
import com.getlipa.eventstore.identifier.Id;

public abstract class Context {

    public static Context from(final String name) {
        if (name == null || name.isEmpty()) {
            return new GlobalContext();
        }
        return new NamedContext(name);
    }

    public abstract Id extractAggregateId(AnyEvent event);

    public abstract Selector createSelector();

    public abstract Selector createSelector(final Id aggregateId);
}
