package com.getlipa.eventstore.core.projection.projector;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.proto.ProtoCodec;
import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Recorder
public class CodecRecorder {

    public BeanContainerListener registerCodecs() {
        final var cdi = Arc.container();
        cdi.instance(Vertx.class)
                .get().eventBus()
                .unregisterCodec(Projector.CODEC)
                .registerCodec(ProtoCodec.create(
                        Projector.CODEC,
                        AnyEvent::toProto,
                        Event::from
                ));
        return beanContainer -> beanContainer.beanInstance(Vertx.class);
    }
}