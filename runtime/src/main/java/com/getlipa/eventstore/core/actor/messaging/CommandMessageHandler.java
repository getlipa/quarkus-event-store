package com.getlipa.eventstore.core.actor.messaging;

import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class CommandMessageHandler<T extends Message> implements MessageHandler {

    private final Object actorBean;

    private final Map<Class<Message>, Method> handlerByPayload;

    public static <T extends Message> CommandMessageHandler<T> create(Object bean) {
        return new CommandMessageHandler<>(bean, extractCommandHandlers(bean));
    }

    static Map<Class<Message>, Method> extractCommandHandlers(Object bean) {
        final var handlerByPayload = new HashMap<Class<Message>, Method>();
        for (final var method : bean.getClass().getSuperclass().getDeclaredMethods()) {
            final var parameterTypes = method.getAnnotatedParameterTypes();
            if (parameterTypes.length != 1) {
                log.trace("Method ignored as command handler due to wrong parameter count: " + method);
                continue;
            }
            ParameterizedType parameterType = null;
            try {
                parameterType = (ParameterizedType) parameterTypes[0].getType();
            } catch (ClassCastException ignored) {
            }
            if (parameterType == null || !Command.class.equals(parameterType.getRawType())) {
                log.trace("Method ignored as command handler due to wrong parameter type: " + method);
                continue;
            }
            final Class<Message> payloadClass;
            try {
                payloadClass = (Class<Message>) parameterType.getActualTypeArguments()[0];
            } catch (ClassCastException e) {
                throw new IllegalStateException("Illegal command handler parameter: " + parameterType);
            }
            if (handlerByPayload.containsKey(payloadClass)) {
                throw new IllegalStateException("Ambiguous command handler for payload: " + payloadClass); // TODO: pass actor method name to resolve?
            }
            handlerByPayload.put(payloadClass, method);
        }
        return handlerByPayload;
    }

    public Object invoke(Command<?> command) throws InvocationTargetException, IllegalAccessException {
        final var handler = handlerByPayload.get(command.getPayload().get().getClass()); // FIXME
        if (handler == null) {
            throw new IllegalStateException("No handler defined for command: " + command);
        }
        return handler.invoke(actorBean, command.withOrigin(UUID.randomUUID())); // TODO: better origin handling
    }

    @SneakyThrows
    public Object handle(Command<?> message) {
        return invoke(message);
    }
}
