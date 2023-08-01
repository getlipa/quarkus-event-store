package com.getlipa.eventstore.core.proto;

import com.google.protobuf.Message;

public abstract class ProtoEncodable<T extends Message> {

    protected abstract T encodeToProto();

    public static abstract class Builder<T, P extends Message> {

        protected abstract T decodeFromProto(P proto);

    }
}
