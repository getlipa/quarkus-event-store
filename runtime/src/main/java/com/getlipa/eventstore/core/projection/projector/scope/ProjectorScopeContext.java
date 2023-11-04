package com.getlipa.eventstore.core.projection.projector.scope;

import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import io.quarkus.arc.InjectableContext;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ProjectorScopeContext implements InjectableContext {

    static final Map<ProjectionTarget.Id, ProjectorScope> scopes = new ConcurrentHashMap<>();

    static final ThreadLocal<ProjectorScope> ACTIVE_SCOPE_ON_THREAD = new ThreadLocal<>();

    @Override
    public Class<? extends Annotation> getScope() {
        return ProjectorScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        return current().get(contextual, creationalContext);
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        return current().get(contextual);
    }

    @Override
    public boolean isActive() {
        return ACTIVE_SCOPE_ON_THREAD.get() != null;
    }

    @Override
    public void destroy(Contextual<?> contextual) {
        current().destroy(contextual);
    }

    @Override
    public void destroy() {
        current().destroy();
    }

    @Override
    public ContextState getState() {
        return () -> {
            final var activeScope = ACTIVE_SCOPE_ON_THREAD.get();
            if (activeScope == null) {
                return Collections.emptyMap();
            }
            return activeScope.getContextualInstances();
        };
    }

    public static ProjectorScope get(ProjectionTarget.Id id) {
        return scopes.computeIfAbsent(id, k -> ProjectorScope.create(id));
    }
    public static ProjectorScope get(String projectorType, String projectorId) {
        return get(ProjectionTarget.Id.create(projectorType, projectorId));
    }

    public static ProjectorScope current() {
        final var scope = ACTIVE_SCOPE_ON_THREAD.get();
        if (scope == null) {
            throw new ContextNotActiveException();
        }
        return scope;
    }

    static <R> R runScoped(ProjectorScope scope, Supplier<R> supplier) {
        final var previous = ACTIVE_SCOPE_ON_THREAD.get();
        ACTIVE_SCOPE_ON_THREAD.set(scope);
        //ContextLocals.put("message", "hello");
        //try (final var mdc = MDC.putCloseable("projector", scope.getActorId().toString())){
        try {
            return supplier.get();
        } finally {
            ACTIVE_SCOPE_ON_THREAD.set(previous);
        }
    }
}