package com.getlipa.eventstore.core.actor;

import com.getlipa.eventstore.core.CdiUtil;
import com.getlipa.eventstore.core.actor.cdi.Actor;
import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.cdi.ActorScopeContext;
import jakarta.enterprise.inject.Instance;

import java.util.function.Consumer;
import java.util.function.Function;

public class Gateway<T> {

    private final Instance<T> interceptedActor;

    private final String actorType;

    public Gateway(Instance<T> interceptedActor) {
        this.interceptedActor = interceptedActor;
        final var bean = interceptedActor.getHandle().getBean();
        actorType = CdiUtil.qualifier(Actor.Type.class, interceptedActor)
                .map(Actor.Type::value)
                .orElseThrow(() -> new IllegalStateException("Actor does not specify type: " + bean.getBeanClass()));
    }

    public <R> R compute(Function<T, R> function) {
        return compute(ActorId.createDefault(actorType), function);
    }

    public void run(Consumer<T> consumer) {
        compute(actor -> {
            consumer.accept(actor);
            return Void.class;
        });
    }

    public void run(String actorId, Consumer<T> consumer) {
        compute(actorId, t -> {
            consumer.accept(t);
            return Void.class;
        });
    }
    public <R> R compute(String actorId, Function<T, R> function) {
        return compute(ActorId.create(actorType, actorId), function);
    }

    <R> R compute(ActorId actorId, Function<T, R> function) {
        return ActorScopeContext.get(actorId).compute(() -> function.apply(interceptedActor.get()));
    }
}
