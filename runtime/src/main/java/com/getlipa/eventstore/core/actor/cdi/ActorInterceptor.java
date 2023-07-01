package com.getlipa.eventstore.core.actor.cdi;

import com.getlipa.eventstore.core.actor.messaging.Command;
import com.getlipa.eventstore.core.actor.messaging.MessageDelivery;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Actor.InterceptorBinding
@Interceptor
@Slf4j
@RequiredArgsConstructor
@Priority(0)
public class ActorInterceptor {

    private final Vertx vertx;

    @AroundInvoke
    Object invokeCommand(InvocationContext context) throws Exception {
        final var method = context.getMethod();
        final var commandParameter = context.getParameters().length == 1 ? context.getParameters()[0] : null;
        if (!(commandParameter instanceof Command)) {
            log.trace("Forwarding non-command method invocation: " + method);
            return context.proceed();
        }
        final var command = (Command<?>) commandParameter;
        if (command.getOrigin() != null) {
            log.trace("Command is meant to be invoked locally: " + method);
            return context.proceed();
        }
        return invokeThroughEventBus(command, context);
    }

    private Object invokeThroughEventBus(Command<?> command, InvocationContext context) throws ExecutionException, InterruptedException {
        final var future = MessageDelivery.create(command, ActorScopeContext.current().getActorId()).deliver(vertx);
        if (context.getMethod().getReturnType().isInstance(Future.class)) {
            return future;
        }
        final var ret = new CompletableFuture<Message>();
        future.onSuccess(ret::complete)
                .onFailure(ret::completeExceptionally);
        return ret.get();
    }

}
