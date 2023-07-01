package com.getlipa.eventstore.core.proto;

import com.getlipa.eventstore.common.Common;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class PayloadParser {

    private final Map<UUID, Parser<? extends Message>> typeMap = new HashMap<>();

    public void register(UUID type, Parser<? extends Message> parser) {
        typeMap.put(type, parser);
        log.trace("Payload parser registered: {} / {}", type, parser.getClass().getName());
    }
    public Message parse(Common.Payload payload) {
        return parse(ProtoUtil.toUUID(payload.getType()), payload.getData().toByteArray());
    }

    public Message parse(UUID type, byte[] payload) {
        final var parser = typeMap.get(type);
        if (parser == null) {
            throw new IllegalStateException("No parser registered for payload type: " + type);
        }
        try {
            return parser.parseFrom(payload);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalStateException("Unable to parse payload type: " + type);
        }
    }
}
