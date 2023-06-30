package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.core.proto.PayloadClassRecorder;
import com.google.protobuf.Message;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

class EventStoreProcessor {

    private static final Logger log = LoggerFactory.getLogger(EventStoreProcessor.class);

    private static final String FEATURE = "event-store";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @Record(STATIC_INIT)
    @BuildStep
    public void recordApplicationPayloadParsers(
            ApplicationIndexBuildItem applicationIndexBuildItem,
            BuildProducer<BeanContainerListenerBuildItem> containerListenerProducer,
            PayloadClassRecorder recorder
    ) {
        final var classLoader = Thread.currentThread().getContextClassLoader();
        for (final var classInfo : applicationIndexBuildItem.getIndex().getKnownClasses()) {
            final Class<?> clazz;
            try {
                clazz = classLoader.loadClass(classInfo.name().toString());
            } catch (ClassNotFoundException e) {
                log.warn("Unable to load indexed class: {}", classInfo.name().toString());
                continue;
            }
            if (!Message.class.isAssignableFrom(clazz)) {
                continue;
            }
            containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(clazz)));
        }
    }
}
