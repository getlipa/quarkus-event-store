package com.getlipa.eventstore.core.actor.cdi;

import io.quarkus.arc.ContextInstanceHandle;
import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.impl.ContextInstanceHandleImpl;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActorScope {

    @Getter
    private final ActorId actorId;

    private final Map<Contextual<?>, ContextInstanceHandle<?>> instances;

    public static ActorScope create(ActorId actorId) {
        return new ActorScope(actorId, new ConcurrentHashMap<>());
    }

    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (creationalContext == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        ContextInstanceHandle<T> contextInstanceHandle = (ContextInstanceHandle<T>) instances.computeIfAbsent(
                contextual,
                c -> {
                    final var createdInstance = contextual.create(creationalContext);
                    return new ContextInstanceHandleImpl<>(
                            (InjectableBean<T>) contextual,
                            createdInstance,
                            creationalContext
                    );
                });
        return contextInstanceHandle.get();
    }

    public <T> T get(Contextual<T> contextual) {
        @SuppressWarnings("unchecked")
        ContextInstanceHandle<T> contextInstanceHandle = (ContextInstanceHandle<T>) instances.get(contextual);
        if (contextInstanceHandle == null) {
            return null;
        }
        return contextInstanceHandle.get();
    }

    public void destroy(Contextual<?> contextual) {
        ContextInstanceHandle<?> contextInstanceHandle = instances.get(contextual);
        if (contextInstanceHandle != null) {
            contextInstanceHandle.destroy();
        }
    }

    public void destroy() {
        instances.values().forEach(ContextInstanceHandle::destroy);
        ActorScopeContext.scopes.remove(actorId);
    }

    public Map<InjectableBean<?>, Object> getContextualInstances() {
        Map<Contextual<?>, ContextInstanceHandle<?>> activeScope = instances;

        if (activeScope != null) {
            return activeScope.values().stream()
                    .collect(Collectors.toMap(ContextInstanceHandle::getBean, ContextInstanceHandle::get));
        }
        return Collections.emptyMap();
    }

    public <R> R compute(Function<ActorScope, R> handler) {
        return ActorScopeContext.runScoped(this, () -> handler.apply(this));
    }


    public <R> R compute(Supplier<R> supplier) {
        return compute(scope -> supplier.get());
    }

    public ActorScope unwrap() {
        return this;
    }
}