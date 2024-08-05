package com.getlipa.eventstore.projection.projector;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.subscriptions.Projections;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.impl.SerializableUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ObjectInputStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventCodec implements MessageCodec<AnyEvent, AnyEvent> {

    public static final String NAME = "event";

    public static EventCodec create() {
        return new EventCodec();
    }

    @Override
    public void encodeToWire(Buffer buffer, AnyEvent wrapped) {
        var bytes = SerializableUtils.toBytes(wrapped.toProto());
        buffer.appendInt(bytes.length);
        buffer.appendBytes(bytes);
    }

    @Override
    public AnyEvent decodeFromWire(int pos, Buffer buffer) {
        var length = buffer.getInt(pos);
        pos += 4;
        var bytes = buffer.getBytes(pos, pos + length);
        return Event.from(((Projections.Event) SerializableUtils.fromBytes(bytes, ObjectInputStream::new)));
    }

    @Override
    public AnyEvent transform(final AnyEvent wrapped) {
        return wrapped;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }

    @Slf4j
    @Recorder
    public static class CodecRecorder {

        public BeanContainerListener registerCodecs() {
            final var cdi = Arc.container();
            cdi.instance(Vertx.class)
                    .get().eventBus()
                    .unregisterCodec(NAME)
                    .registerCodec(create());
            return beanContainer -> beanContainer.beanInstance(Vertx.class);
        }
    }
}
