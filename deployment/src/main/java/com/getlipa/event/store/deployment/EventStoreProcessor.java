package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.Jobs;
import com.getlipa.eventstore.Mutex;
import com.getlipa.eventstore.projection.projector.EventCodec;
import com.getlipa.eventstore.event.payload.PayloadDeserializer;
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

import com.getlipa.eventstore.persistence.postgres.JpaEvent;
import com.getlipa.eventstore.persistence.postgres.PostgresEventPersistence;
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
            PayloadDeserializer.PayloadClassRecorder recorder
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
            containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record((Class<? extends Message>) clazz)));
        }
    }

    @Record(STATIC_INIT)
    @BuildStep
    public void recordInternalPayloadParsers(
            BuildProducer<BeanContainerListenerBuildItem> containerListenerProducer,
            PayloadDeserializer.PayloadClassRecorder recorder
    ) {
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.Event.class)));
        //containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Example.Simple.class)));
        //containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Example.Other.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.CatchUpStarted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.CatchUpCompleted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.CheckpointReached.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.IntermediateCheckpointReached.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.ListeningStarted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Projections.ListeningStopped.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Jobs.JobStarted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Jobs.JobCompleted.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Jobs.JobFailed.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Jobs.CheckpointReached.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Mutex.LockAcquired.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Mutex.LockReleased.class)));
    }

    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    @Record(RUNTIME_INIT)
    @BuildStep
    public void registerVertxCodecs(
            EventCodec.CodecRecorder codecRecorder

    ) {
        codecRecorder.registerCodecs();
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(PayloadDeserializer.class)
                .addBeanClass(PostgresEventPersistence.class)
                .addBeanClass(JpaEvent.class)
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
