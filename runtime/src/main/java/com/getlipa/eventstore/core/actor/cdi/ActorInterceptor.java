package com.getlipa.eventstore.core.actor.cdi;

import com.getlipa.eventstore.core.actor.messaging.AnyMsg;
import com.getlipa.eventstore.core.actor.messaging.Msg;
import com.getlipa.eventstore.core.actor.messaging.MessageDelivery;
import com.getlipa.eventstore.core.actor.messaging.MsgHandlerInvoker;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Actor.InterceptorBinding
@Interceptor
@Slf4j
@RequiredArgsConstructor
@Priority(0)
public class ActorInterceptor {

    private final Vertx vertx;

    private static final ThreadLocal<Boolean> isLocal = ThreadLocal.withInitial(() -> false);

    @AroundInvoke
    Object invokeMsgHandler(InvocationContext context) throws Exception {
        final var method = context.getMethod();
        final var msgParameter = context.getParameters().length == 1 ? context.getParameters()[0] : null;
        if (!(msgParameter instanceof Msg)) {
            log.trace("Forwarding non-msg handler method invocation: " + method);
            return context.proceed();
        }
        final var msg = (AnyMsg) msgParameter;
        if (isLocal.get()) {
            log.trace("Msg handler is meant to be invoked locally: " + method);
            return context.proceed();
        }
        return invokeThroughEventBus(msg, context);
    }

    private Object invokeThroughEventBus(AnyMsg msg, InvocationContext context) throws ExecutionException, InterruptedException {
        final var future = MessageDelivery.create(msg, ActorScopeContext.current().getActorId()).deliver(vertx);
        if (context.getMethod().getReturnType().isInstance(Future.class)) {
            return future;
        }
        final var ret = new CompletableFuture<Message>();
        future.onSuccess(ret::complete)
                .onFailure(ret::completeExceptionally);
        return ret.get();
    }

    public static Object invokeLocally(MsgHandlerInvoker msgHandlerInvoker, AnyMsg msg) throws InvocationTargetException, IllegalAccessException {
        isLocal.set(true);
        try {
            return msgHandlerInvoker.invoke(msg);
        } finally {
            isLocal.set(false);
        }
    }

}
