package com.getlipa.eventstore.core.actor.messaging;

import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class MsgHandlerInvoker {

    private final Object actorBean;

    private final Map<Class<Message>, Method> handlerByPayload;

    public static MsgHandlerInvoker create(Object bean) {
        return new MsgHandlerInvoker(bean, extractMsgHandlers(bean));
    }

    static Map<Class<Message>, Method> extractMsgHandlers(Object bean) {
        final var handlerByPayload = new HashMap<Class<Message>, Method>();
        for (final var method : bean.getClass().getSuperclass().getDeclaredMethods()) {
            final var parameterTypes = method.getAnnotatedParameterTypes();
            if (parameterTypes.length != 1) {
                log.trace("Method ignored as msg handler due to wrong parameter count: " + method);
                continue;
            }
            ParameterizedType parameterType = null;
            try {
                parameterType = (ParameterizedType) parameterTypes[0].getType();
            } catch (ClassCastException ignored) {
            }
            if (parameterType == null || !Msg.class.equals(parameterType.getRawType())) {
                log.trace("Method ignored as msg handler due to wrong parameter type: " + method);
                continue;
            }
            final Class<Message> payloadClass;
            try {
                payloadClass = (Class<Message>) parameterType.getActualTypeArguments()[0];
            } catch (ClassCastException e) {
                throw new IllegalStateException("Illegal msg handler parameter: " + parameterType);
            }
            if (handlerByPayload.containsKey(payloadClass)) {
                throw new IllegalStateException("Ambiguous msg handler for payload: " + payloadClass); // TODO: pass actor method name to resolve?
            }
            handlerByPayload.put(payloadClass, method);
        }
        return handlerByPayload;
    }

    public Object invoke(AnyMsg msg) throws InvocationTargetException, IllegalAccessException {
        final var handler = handlerByPayload.get(msg.getPayload().get().getClass()); // FIXME
        if (handler == null) {
            throw new IllegalStateException("No handler defined for msg: " + msg);
        }
        return handler.invoke(actorBean, msg);
    }
}
