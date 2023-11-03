package com.getlipa.eventstore.core.proto;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
@Recorder
@ApplicationScoped
public class PayloadClassRecorder {

    private static final String DESCRIPTOR_GETTER_METHOD_NAME = "getDescriptor";

    private static final String PARSER_FACTORY_METHOD_NAME = "parser";

    @SuppressWarnings("unchecked")
    public BeanContainerListener record(Class<?> messageClass) {
        final Descriptors.Descriptor descriptor;
        try {
            descriptor = (Descriptors.Descriptor) messageClass.getMethod(DESCRIPTOR_GETTER_METHOD_NAME)
                    .invoke(null);
            final var parser = (Parser<Message>) messageClass.getMethod(PARSER_FACTORY_METHOD_NAME).invoke(null);
            return beanContainer -> beanContainer.beanInstance(PayloadParser.class)
                    .register(ProtoUtil.toUUID(descriptor), parser);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
           throw new IllegalStateException("Failed to record payload parser.", e);
        }
    }
}
