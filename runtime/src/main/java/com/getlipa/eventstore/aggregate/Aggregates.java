package com.getlipa.eventstore.aggregate;

import com.getlipa.eventstore.aggregate.hydration.AggregateHydratorFactory;
import com.getlipa.eventstore.aggregate.cdi.AggregateType;
import com.getlipa.eventstore.identifier.Id;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Aggregates<T> {

    private final AggregateHydratorFactory aggregateHydratorFactory;

    public Aggregate<T> get(Id id) {
        final var eventTipHolder = EventTipHolder.<T>create();
        return new Aggregate<>(
                id,
                aggregateHydratorFactory.create(id, eventTipHolder),
                eventTipHolder
        );
    }

    @RequiredArgsConstructor
    static class Producer {

        @Inject
        @Any
        Instance<AggregateHydratorFactory> factories;

        @Produces
        @Dependent
        <T> Aggregates<T> produceProjectedStreams(InjectionPoint injectionPoint) {
            return new Aggregates<>(factories.select(AggregateType.Literal.from(injectionPoint)).get());
        }
    }
}
