package com.getlipa.eventstore.core.projection.trgt;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.projector.scope.ProjectorScopeContext;
import com.getlipa.eventstore.core.projection.projector.scope.ProjectorScoped;
import com.getlipa.eventstore.core.projection.trgt.context.InitializeContext;
import com.getlipa.eventstore.core.projection.trgt.context.ApplyContext;
import com.getlipa.eventstore.core.projection.trgt.context.RefreshContext;
import com.getlipa.eventstore.core.projection.trgt.eventhandler.EventHandlerInvoker;
import io.vertx.core.Future;
import jakarta.enterprise.inject.Produces;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class ProjectionTarget<T> {

    @Getter
    private final ProjectionMetadata projectionMetadata;

    @Getter
    private final ProjectionTarget.Id id;

    private final Iterable<Middleware<T>> middlewares;

    private final EventHandlerInvoker eventHandlerInvoker;

    private final Supplier<T> targetSupplier;

    private T target;

    private Future<T> initFuture;

    private ProjectionStep<T> firstStep;

    @Getter
    private AnyEvent eventTip;

    public void reset() {
        firstStep = null;
        target = null;
        initFuture = null;
    }

    public T get() {
        return target;
    }

    public Future<T> initialized() {
        if (initFuture != null) {
           return initFuture;
        }
        target = targetSupplier.get();
        firstStep = ProjectionTarget.Middleware.chained(
                eventHandlerInvoker.createProjectionStep(target),
                middlewares
        );
        final var context = new InitializeContext<>(
                this,
                firstStep
        );
        return initFuture = context.proceed();
    }

    public Future<T> refreshed() {
        final var context = new RefreshContext<>(
                this,
                firstStep
        );
        return initialized()
                .flatMap(target -> context.proceed());
    }

    public Future<T> apply(AnyEvent event) {
        initialized();
        final var context = new ApplyContext<>(
                this,
                firstStep,
                event
        );
        return context.proceed()
                .onSuccess(result -> {
                    eventTip = event;
                });
    }

    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class Id {

        private static final char SEPARATOR = ':';

        private final String type;

        private final String name;

        public static Id createDefault(String type) {
            return create(type, "_default_");
        }

        public static Id create(@NonNull String id) {
            final var separatorIndex = id.indexOf(SEPARATOR);
            if (separatorIndex < 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid id, expected <type>%s<name> but got '%s'", SEPARATOR, id)
                );
            }
            return create(id.substring(0, separatorIndex), id.substring(separatorIndex + 1));
        }

        public static Id create(String type, String name) {
            return new Id(type, name);
        }

        @Override
        public String toString() {
            return type + SEPARATOR + name;
        }

    }

    public static class Producer {

        @Produces
        @ProjectorScoped
        public Id produce() {
            return ProjectorScopeContext.current().getId();
        }
    }

    @Slf4j
    public abstract static class Middleware<T> {

        @SafeVarargs
        public static <T> ProjectionStep<T> chained(
                final ProjectionStep<T> projected,
                final Middleware<T>... middlewares
        ) {
            return chained(projected, List.of(middlewares));
        }

        public static <T> ProjectionStep<T> chained(
                final ProjectionStep<T> projected,
                final Iterable<Middleware<T>> middlewares
        ) {
            ProjectionStep<T> next = projected;
            for (final var middleware : middlewares) {
                next = new ProjectionTarget.MiddlewareStep<>(middleware, next);
            }
            return next;
        }

        protected Future<T> init(InitializeContext<T> context) {
            return context.proceed();
        }

        public Future<T> project(ApplyContext<T> context) {
            return context.proceed();
        }

        public Future<T> refresh(RefreshContext<T> context) {
            return context.proceed();
        }
    }

    @RequiredArgsConstructor
    static class MiddlewareStep<T> extends ProjectionStep<T> {

        private final Middleware<T> middleware;

        private final ProjectionStep<T> nextStep;

        @Override
        public Future<T> skip() {
            return nextStep.skip();
        }

        @Override
        public Future<T> initialize(InitializeContext<T> context) {
            return middleware.init(context.advance(nextStep));
        }

        @Override
        public Future<T> apply(ApplyContext<T> context) {
            return middleware.project(context.withNext(nextStep));
        }

        @Override
        public Future<T> refresh(RefreshContext<T> context) {
            return middleware.refresh(context.advance(nextStep));
        }
    }
}
