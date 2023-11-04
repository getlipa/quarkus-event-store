package com.getlipa.eventstore.core.projection.projector.scope;

import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProjectorScope {

    @Getter
    private final ProjectionTarget.Id id;

    private final Map<Contextual<?>, ContextInstanceHandle<?>> instances;

    public static ProjectorScope create(ProjectionTarget.Id id) {
        return new ProjectorScope(id, new ConcurrentHashMap<>());
    }

    public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext) {
        if (creationalContext == null) {
            return null;
        }
        if (!instances.containsKey(contextual)) {
            instances.put(contextual, new ContextInstanceHandleImpl<>(
                    (InjectableBean<T>) contextual,
                    contextual.create(creationalContext),
                    creationalContext
            ));
        }
        @SuppressWarnings("unchecked")
        ContextInstanceHandle<T> contextInstanceHandle = (ContextInstanceHandle<T>) instances.get(contextual);
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
        ProjectorScopeContext.scopes.remove(id);
    }

    public Map<InjectableBean<?>, Object> getContextualInstances() {
        Map<Contextual<?>, ContextInstanceHandle<?>> activeScope = instances;

        if (activeScope != null) {
            return activeScope.values().stream()
                    .collect(Collectors.toMap(ContextInstanceHandle::getBean, ContextInstanceHandle::get));
        }
        return Collections.emptyMap();
    }

    public <R> R compute(Function<ProjectorScope, R> handler) {
        return ProjectorScopeContext.runScoped(this, () -> handler.apply(this));
    }


    public <R> R compute(Supplier<R> supplier) {
        return compute(scope -> supplier.get());
    }

    public void run(Runnable runnable) {
        compute(() -> {
            runnable.run();
            return null;
        });
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(() -> run(runnable));
    }

    public ProjectorScope unwrap() {
        return this;
    }
}