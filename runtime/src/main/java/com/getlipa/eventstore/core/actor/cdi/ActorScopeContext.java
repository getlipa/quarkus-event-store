package com.getlipa.eventstore.core.actor.cdi;

import io.quarkus.arc.InjectableContext;
import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import org.slf4j.MDC;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ActorScopeContext implements InjectableContext {

    static final Map<ActorId, ActorScope> scopes = new ConcurrentHashMap<>();

    static final ThreadLocal<ActorScope> ACTIVE_SCOPE_ON_THREAD = new ThreadLocal<>();

    @Override
    public Class<? extends Annotation> getScope() {
        return ActorScoped.class;
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

    public static ActorScope get(ActorId actorId) {
        return scopes.computeIfAbsent(actorId, k -> ActorScope.create(actorId));
    }
    public static ActorScope get(String actorType, String actorId) {
        return get(ActorId.create(actorType, actorId));
    }

    public static ActorScope current() {
        final var scope = ACTIVE_SCOPE_ON_THREAD.get();
        if (scope == null) {
            throw new ContextNotActiveException();
        }
        return scope;
    }

    static <R> R runScoped(ActorScope scope, Supplier<R> supplier) {
        final var previous = ACTIVE_SCOPE_ON_THREAD.get();
        ACTIVE_SCOPE_ON_THREAD.set(scope);
        try (final var mdc = MDC.putCloseable("actor", scope.getActorId().toString())){
            return supplier.get();
        } finally {
            ACTIVE_SCOPE_ON_THREAD.set(previous);
        }
    }
}