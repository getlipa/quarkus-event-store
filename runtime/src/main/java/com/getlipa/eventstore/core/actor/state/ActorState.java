package com.getlipa.eventstore.core.actor.state;

import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.messaging.MessageDelivery;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;

public abstract class ActorState {

    public static Future<ActorState> determine(ActorId actorId, Vertx vertx) {
        return vertx.sharedData().getCounter(actorId.toString())
                .flatMap(Counter::get)
                .map(status -> ActorState.from(status, vertx));
    }

    public static ActorState from(
            Long status,
            Vertx vertx
    ) {
        if (status == null || status == 0) {
            return new NoActor(vertx);
        }
        return new ActorReady(vertx, status);
    }

    abstract public <R extends Message> Future<R> process(MessageDelivery message);

}
