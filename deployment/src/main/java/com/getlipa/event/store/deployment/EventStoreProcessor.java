package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.core.proto.PayloadClassRecorder;
import com.google.protobuf.Message;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ApplicationIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import org.jboss.jandex.AnnotationValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import com.getlipa.eventstore.actors.Actors;
import com.getlipa.eventstore.core.actor.messaging.CodecRecorder;
import com.getlipa.eventstore.core.actor.messaging.CommandCodec;
import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.actor.messaging.ResultCodec;
import com.getlipa.eventstore.core.actor.cdi.Actor;
import com.getlipa.eventstore.core.actor.GatewayProducer;
import com.getlipa.eventstore.core.actor.cdi.ActorInterceptor;
import com.getlipa.eventstore.core.actor.cdi.ActorScopeContext;
import com.getlipa.eventstore.core.actor.cdi.ActorScoped;
import com.getlipa.eventstore.core.actor.cdi.ActorIdProducer;
import com.getlipa.eventstore.core.persistence.postgres.JpaEvent;
import com.getlipa.eventstore.core.persistence.postgres.PostgresEventPersistence;
import com.getlipa.eventstore.core.proto.PayloadParser;
import com.getlipa.eventstore.core.subscription.EventDispatcher;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.deployment.ContextRegistrationPhaseBuildItem;
import io.quarkus.arc.deployment.CustomScopeBuildItem;
import io.quarkus.arc.deployment.StereotypeRegistrarBuildItem;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.deployment.annotations.Consume;
import org.jboss.jandex.DotName;

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
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Actors.Command.class)));
        containerListenerProducer.produce(new BeanContainerListenerBuildItem(recorder.record(Actors.Result.class)));
    }

    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    @Record(RUNTIME_INIT)
    @BuildStep
    public void registerVertxCodecs(
            CodecRecorder recorder

    ) {
        recorder.registerCodecs();
    }


    @BuildStep
    public ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem transactionContext(ContextRegistrationPhaseBuildItem contextRegistrationPhase) {
        return new ContextRegistrationPhaseBuildItem.ContextConfiguratorBuildItem(contextRegistrationPhase.getContext()
                .configure(ActorScoped.class).normal().contextClass(ActorScopeContext.class));
    }

    @BuildStep
    public CustomScopeBuildItem registerScope() {
        return new CustomScopeBuildItem(ActorScoped.class);
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeans() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(ActorIdProducer.class)
                .addBeanClass(GatewayProducer.class)
                .addBeanClass(PayloadParser.class)
                .addBeanClass(ActorInterceptor.class)
                .addBeanClass(CommandCodec.class)
                .addBeanClass(ResultCodec.class)
                .addBeanClass(EventStore.class)
                .addBeanClass(EventDispatcher.class)
                .addBeanClass(PostgresEventPersistence.class)
                .addBeanClass(JpaEvent.class)
                .setUnremovable()
                .build();
    }

    @BuildStep
    public StereotypeRegistrarBuildItem addStereotypes() {
        return new StereotypeRegistrarBuildItem(() -> Set.of(
                DotName.createSimple(Actor.class)
        ));
    }

    @BuildStep
    AnnotationsTransformerBuildItem transformActorBeans() {
        return new AnnotationsTransformerBuildItem(AnnotationsTransformer.appliedToClass()
                .whenClass(c -> c.hasAnnotation(Actor.class))
                .transform(c -> {
                            final var actorType = c.getTarget().annotation(Actor.class).value().asString();
                            c.transform()
                                    .add(ActorScoped.class)
                                    .add(Actor.Qualifier.class)
                                    .add(Actor.InterceptorBinding.class)
                                    .add(Actor.Type.class, AnnotationValue.createStringValue("value", actorType))
                                    .done();
                        }
                ));
    }
}
