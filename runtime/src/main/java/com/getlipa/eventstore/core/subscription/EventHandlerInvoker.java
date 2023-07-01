package com.getlipa.eventstore.core.subscription;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.subscription.cdi.EventHandler;
import com.google.protobuf.Message;
import io.quarkus.arc.ClientProxy;
import io.quarkus.arc.Subclass;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class EventHandlerInvoker implements EventProcessor {

    private final Object bean;

    private final Map<Class<Message>, Method> handlerByPayload;

    public static EventHandlerInvoker create(Object bean) {
        return new EventHandlerInvoker(bean, extractEventHandlers(bean));
    }

    static Map<Class<Message>, Method> extractEventHandlers(Object bean) {
        final var handlerByPayload = new HashMap<Class<Message>, Method>();
        var clazz = bean.getClass();
        if (bean instanceof ClientProxy || bean instanceof Subclass) {
            clazz = clazz.getSuperclass();
        }
        for (final var method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(EventHandler.class)) {
                continue;
            }
            final var parameterTypes = method.getAnnotatedParameterTypes();
            if (parameterTypes.length != 1) {
                throw new IllegalStateException("Invalid amount of parameters for event handler method: " + method);
            }
            final ParameterizedType parameterType;
            try {
                parameterType = (ParameterizedType) parameterTypes[0].getType();
            } catch (ClassCastException e) {
                throw new IllegalStateException("Illegal event handler parameter: " + parameterTypes[0].getType());
            }
            if (!Event.class.equals(parameterType.getRawType())) {
                throw new IllegalStateException("Illegal event handler parameter: " + parameterType);
            }
            final Class<Message> payloadClass;
            try {
                payloadClass = (Class<Message>) parameterType.getActualTypeArguments()[0];
            } catch (ClassCastException e) {
                throw new IllegalStateException("Illegal event handler parameter: " + parameterType);
            }
            if (handlerByPayload.containsKey(payloadClass)) {
                throw new IllegalStateException("Ambiguous event handler for payload: " + payloadClass);
            }
            handlerByPayload.put(payloadClass, method);
        }
        return handlerByPayload;
    }

    @Override
    public Future<Void> process(AnyEvent event) {
        final var handler = handlerByPayload.get(event.payload().getClass());
        if (handler == null) {
            return Future.succeededFuture();
        }
        try {
            return (Future<Void>) handler.invoke(bean, event); // TODO: use callback for map, make the checks before
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
