package com.getlipa.eventstore.core.proto;

import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.impl.SerializableUtils;

import java.io.ObjectInputStream;

public abstract class ProtoCodec<T, P extends GeneratedMessageV3> implements MessageCodec<T, T> {

    @Override
    public void encodeToWire(Buffer buffer, T wrapped) {
        var bytes = SerializableUtils.toBytes(toProto(wrapped));
        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        var length = buffer.getInt(pos);
        pos += 4;
        var bytes = buffer.getBytes(pos, pos + length);
        return toWrapped((P) SerializableUtils.fromBytes(bytes, ObjectInputStream::new));
    }

    @Override
    public T transform(T wrapped) {
        return wrapped;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

    protected abstract P toProto(T wrapped);

    protected abstract T toWrapped(P proto);

}
