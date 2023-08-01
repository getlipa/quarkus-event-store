package com.getlipa.eventstore.core.actor.messaging;

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
                .unregisterCodec(Msg.CODEC)
                .registerCodec(ProtoCodec.create(Msg.CODEC, Msg::create));
        return beanContainer -> beanContainer.beanInstance(Vertx.class);
    }
}