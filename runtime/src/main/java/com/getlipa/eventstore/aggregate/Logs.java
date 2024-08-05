package com.getlipa.eventstore.aggregate;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.cdi.AggregateType;
import com.getlipa.eventstore.aggregate.context.NamedContext;
import com.getlipa.eventstore.aggregate.hydration.AggregateHydratorFactory;
import com.getlipa.eventstore.identifier.Id;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Logs<T> {

    private final NamedContext context;

    private final AggregateHydratorFactory aggregateHydratorFactory;

    private final EventStore eventStore;

    public Log<T> get(String id) {
        return get(Id.derive(id));
    }

    public Log<T> get(Id id) {
        final var eventTipHolder = EventTipHolder.<T>create();
        return new Log<>(
                id,
                aggregateHydratorFactory.create(id, eventTipHolder),
                eventTipHolder,
                eventStore.stream(context.createSelector(id))
        );
    }

    @RequiredArgsConstructor
    static class Producer {

        private final EventStore eventStore;

        @Inject
        @Any
        Instance<AggregateHydratorFactory> factories;

        @Inject
        @Any
        Instance<NamedContext> namedContext;

        @Produces
        @Dependent
        <T> Logs<T> produceProjectedLogs(InjectionPoint injectionPoint) {
            final var aggregateTypeQualifier = AggregateType.Literal.from(injectionPoint);
            final var contextInstance = namedContext.select(aggregateTypeQualifier);
            if (contextInstance.isUnsatisfied()) {
                throw new IllegalStateException(String.format(
                        "%s<%s> is invalid. %s<...> can only be used with a named context",
                        Logs.class,
                        injectionPoint.getType(),
                        Logs.class
                ));
            }
            return new Logs<>(
                    contextInstance.get(),
                    factories.select(aggregateTypeQualifier).get(),
                    eventStore
            );
        }
    }
}
