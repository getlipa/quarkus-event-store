package com.getlipa.eventstore.hydration.eventhandler;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.event.payload.AnyPayload;
import com.getlipa.eventstore.hydration.Hydrator;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class EventHandlerInvoker {

    private final Map<Class<Message>, Method> handlerByPayload;

    public static EventHandlerInvoker create(Class<?> beanClass) {
        return new EventHandlerInvoker(extractEventHandlers(beanClass));
    }

    static Map<Class<Message>, Method> extractEventHandlers(Class<?> clazz) {
        final var handlerByPayload = new HashMap<Class<Message>, Method>();
        for (final var method : clazz.getDeclaredMethods()) {
            if (!method.isAnnotationPresent(Apply.class)) {
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

    public Future<Void> invoke(Object bean, AnyEvent event) {
        return Optional.ofNullable(event.getPayload())
                .map(AnyPayload::get)
                .map(payload -> handlerByPayload.get(payload.getClass()))
                .map(handler -> invoke(bean, handler, event))
                .orElseGet(() -> {
                    log.debug("No handler found for event on {}: {}", bean.getClass().getSimpleName(), event);
                    return Future.succeededFuture();
                });
    }

    private Future<Void> invoke(Object bean, Method handler, AnyEvent event) {
        final Object result;
        try {
            result = handler.invoke(bean, event);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(String.format(
                    "An exception occurred when invoking the event handler: %s - %s",
                    e.getTargetException().getClass().getSimpleName(),
                    e.getTargetException().getMessage()
            ));
        }
        if (result == null) {
            return Future.succeededFuture();
        }
        return (Future<Void>) result; // TODO: use callback for map, make the checks before
    }

    @SuppressWarnings("unchecked")
    public <T> Hydrator<T> createHydrator(T target) {
        return new InvokerHydrator<T>(
                this,
                target,
                (Class<T>) target.getClass()
        );
    }


}
