package com.getlipa.eventstore.event.payload;

import com.getlipa.eventstore.identifier.Id;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PayloadDeserializer {

    private static final Map<Id, Parser<? extends Message>> parsers = new HashMap<>();

    public static void register(
            final Descriptors.Descriptor descriptor,
            final Parser<? extends Message> parser
    ) {
        final var id = typeId(descriptor);
        log.debug("Parser registered: {} - {}", descriptor.getFullName(), id.shortUUID());
        parsers.put(id, parser);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Message> T deserialize(final Id typeId, final byte[] serialized) {
        if (!parsers.containsKey(typeId)) {
            throw new IllegalStateException("No parser registered for payload type: " + typeId);
        }
        try {
            return (T) parsers.get(typeId).parseFrom(serialized);
        } catch (ClassCastException e) {
            throw new IllegalStateException(String.format("Payload type mismatch: %s - %s", typeId, e.getMessage()));
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("Unable to deserialize payload type: " + typeId);
        }
    }

    static Id typeId(Descriptors.Descriptor descriptor) {
        return Id.derive(Payload.PAYLOAD_TYPE_NAMESPACE, descriptor.getFullName());
    }

    @Slf4j
    @Recorder
    @ApplicationScoped
    public static class PayloadClassRecorder {

        private static final String DESCRIPTOR_GETTER_METHOD_NAME = "getDescriptor";

        private static final String PARSER_FACTORY_METHOD_NAME = "parser";

        @SuppressWarnings("unchecked")
        public BeanContainerListener record(Class<? extends Message> messageClass) {
            final Descriptors.Descriptor descriptor;
            try {
                descriptor = (Descriptors.Descriptor) messageClass.getMethod(DESCRIPTOR_GETTER_METHOD_NAME)
                        .invoke(null);
                final var parser = (Parser<Message>) messageClass.getMethod(PARSER_FACTORY_METHOD_NAME).invoke(null);
                return beanContainer -> register(descriptor, parser);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
               throw new IllegalStateException("Failed to record payload parser.", e);
            }
        }
    }
}
