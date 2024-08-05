package com.getlipa.eventstore.aggregate.hydration;

import com.getlipa.eventstore.aggregate.middleware.AggregateMiddleware;
import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.hydration.Hydrator;
import com.getlipa.eventstore.aggregate.cdi.AggregateBean;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.hydration.eventhandler.EventHandlerInvoker;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.arc.Unremovable;
import io.quarkus.runtime.annotations.Recorder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class AggregateHydratorFactory {

    private final Context context;

    private final EventHandlerInvoker eventHandlerInvoker;

    private final Bean<Object> aggregateBean;

    private final List<Bean<AggregateMiddleware<?>>> middlewareBeans;

    private final BeanManager beanManager;

    public static AggregateHydratorFactory create(
            final Context context,
            final List<Bean<AggregateMiddleware<?>>> middlewares,
            final Bean<Object> bean,
            final BeanManager beanManager
    ) {
        return new AggregateHydratorFactory(
                context,
                EventHandlerInvoker.create(bean.getBeanClass()),
                bean,
                middlewares,
                beanManager
        );
    }

    public <T> Hydrator<T> create(final Id aggregateId) {
        return create(aggregateId, Collections.emptySet());
    }

    public <T> Hydrator<T> create(final Id aggregateId, AggregateMiddleware<T> anonymousAggregateMiddleware) {
        return create(aggregateId, Set.of(anonymousAggregateMiddleware));
    }

    public <T> Hydrator<T> create(final Id aggregateId, final Collection<? extends AggregateMiddleware<T>> middlewares) {
        final var middlewareInstances = new LinkedList<AggregateMiddleware<T>>(middlewares);
        middlewareBeans.stream()
                .map(bean -> bean.create(beanManager.createCreationalContext(bean)))
                .forEach(instance -> {
                    try {
                        middlewareInstances.add((AggregateMiddleware<T>) instance);
                    } catch (ClassCastException e) {
                        throw new IllegalStateException(
                                String.format(
                                        "Middleware is not compatible with aggregate: %s / %s",
                                        instance.getClass(),
                                        aggregateBean.getClass()
                                ),
                                e
                        );
                    }
                });
        final var anyBean = aggregateBean.create(beanManager.createCreationalContext(aggregateBean));
        final T bean;
        try {
            bean = (T) anyBean;
        } catch (ClassCastException e) {
            throw new IllegalStateException(
                    String.format("Aggregate is of wrong type: %s / %s (expected: %s)", // TODO: Move casting to build time?
                            aggregateId,
                            anyBean.getClass(),
                            aggregateBean.getClass()
                    ),
                    e
            );
        }
        return AggregateHydrator.chained(
                aggregateId,
                context,
                eventHandlerInvoker.createHydrator(bean),
                middlewareInstances
        );
    }

    @Slf4j
    @Unremovable
    @ApplicationScoped
    @RequiredArgsConstructor
    static class Factory {

        @Inject
        Instance<AggregateMiddleware<?>> middlewares;

        @Inject
        @AggregateBean
        Instance<Object> beans;

        @Inject
        BeanManager beanManager;

        public AggregateHydratorFactory create(
                final Context context,
                final Class<Object> aggregateClass,
                final List<Class<AggregateMiddleware<?>>> middlewareClasses
        ) {
            final var middlewareHandles = middlewareClasses.stream()
                    .map(middlewareClass -> middlewares.select(middlewareClass))
                    .map(Instance::getHandle)
                    .map(Instance.Handle::getBean)
                    .collect(Collectors.toUnmodifiableList());
            return AggregateHydratorFactory.create(
                    context,
                    middlewareHandles,
                    beans.select(aggregateClass).getHandle().getBean(),
                    beanManager
            );
        }
    }

    @Recorder
    public static class BeanRecorder {

        public Function<SyntheticCreationalContext<Object>, Object> record(
                final Context context,
                final String aggregateType,
                final String[] middlewares
        ) {
            final var classLoader = Thread.currentThread().getContextClassLoader();
            final Class<?> aggregateClass;
            final var middlewareClasses = new LinkedList<Class<AggregateMiddleware<?>>>();
            try {
                for (var middleware : middlewares) {
                    middlewareClasses.add((Class<AggregateMiddleware<?>>) Class.forName(middleware, true, classLoader));
                }
                aggregateClass = Class.forName(aggregateType, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                        String.format(
                                "Middleware class not found: %s",
                                aggregateType
                        ),
                        e
                );
            }
            return ctx -> CDI.current()
                    .select(AggregateHydratorFactory.Factory.class)
                    .get()
                    .create(context, (Class<Object>) aggregateClass, middlewareClasses);
        }
    }

}
