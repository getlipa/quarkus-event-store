package com.getlipa.eventstore.core.proto;

import com.google.protobuf.GeneratedMessageV3;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.impl.SerializableUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.ObjectInputStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProtoCodec<T, P extends GeneratedMessageV3> implements MessageCodec<T, T> {

    private final String name;

    private final AnyPayload.Encoder<T, P> encoder;

    private final AnyPayload.Decoder<T, P> decoder;


    public static <T, P extends GeneratedMessageV3> ProtoCodec<T, P> create(
            final String name,
            final AnyPayload.Encoder<T, P> encoder,
            final AnyPayload.Decoder<T, P> decoder
    ) {
        return new ProtoCodec<>(name, encoder, decoder);
    }

    @Override
    public void encodeToWire(Buffer buffer, T wrapped) {
        var bytes = SerializableUtils.toBytes(encoder.encode(wrapped));
        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
    }

    @Override
    public T decodeFromWire(int pos, Buffer buffer) {
        var length = buffer.getInt(pos);
        pos += 4;
        var bytes = buffer.getBytes(pos, pos + length);
        return decoder.decode(((P) SerializableUtils.fromBytes(bytes, ObjectInputStream::new)));
    }

    @Override
    public T transform(T wrapped) {
        return wrapped;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
