package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.core.projection.projector.CodecRecorder;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTarget;
import com.getlipa.eventstore.core.proto.PayloadClassRecorder;
import com.getlipa.eventstore.example.event.Example;
import com.getlipa.eventstore.subscriptions.Projections;
import com.google.protobuf.Message;
import io.quarkus.arc.deployment.*;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import com.getlipa.eventstore.core.persistence.postgres.JpaEvent;
import com.getlipa.eventstore.core.persistence.postgres.PostgresEventPersistence;
import com.getlipa.eventstore.core.proto.PayloadParser;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.deployment.PanacheEntityClassBuildItem;
import org.jboss.jandex.ClassInfo;

import java.util.Set;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

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

    @Record(STATIC_INIT)
    @BuildStep
    public void recordInternalPayloadParsers(
            BuildProducer<BeanContainerListenerBuildItem> containerListenerProducer,
            PayloadClassRecorder recorder
    ) {
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.Event.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Example.Simple.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Example.Other.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.CatchUpStarted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.CatchUpCompleted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.CheckpointReached.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.IntermediateCheckpointReached.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.ListeningStarted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.ListeningStopped.class)));
    }

    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    @Record(RUNTIME_INIT)
    @BuildStep
    public void registerVertxCodecs(
            CodecRecorder codecRecorder

    ) {
        codecRecorder.registerCodecs();
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(PayloadParser.class)
                .addBeanClass(PostgresEventPersistence.class)
                .addBeanClass(JpaEvent.class)
                .addBeanClass(ProjectionTarget.Producer.class)
                .setUnremovable()
                .build();
    }

    @BuildStep
    void additionalEntities(CombinedIndexBuildItem index, BuildProducer<PanacheEntityClassBuildItem> entityClasses) {
        for (ClassInfo panacheEntityBaseSubclass : index.getIndex().getAllKnownSubclasses(PanacheEntity.class)) {
            entityClasses.produce(new PanacheEntityClassBuildItem(panacheEntityBaseSubclass));
        }
    }

    @BuildStep
    public StereotypeRegistrarBuildItem addStereotypes() {
        return new StereotypeRegistrarBuildItem(() -> Set.of(
                //DotName.createSimple(Actor.class)
        ));
    }
}
