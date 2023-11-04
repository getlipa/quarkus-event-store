package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.core.UuidGenerator;
import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.event.selector.SelectorFactory;
import com.getlipa.eventstore.core.projection.extension.ExtensionFactory;
import com.getlipa.eventstore.core.projection.extension.ProjectionExtension;
import com.getlipa.eventstore.core.projection.mgmt.ProjectionManager;
import com.getlipa.eventstore.core.projection.trgt.ProjectionTargetFactory;
import com.getlipa.eventstore.core.projection.ProjectionMetadata;
import com.getlipa.eventstore.core.projection.cdi.EventMatcher;
import com.getlipa.eventstore.core.projection.projector.DispatchStrategy;
import com.getlipa.eventstore.core.projection.projector.ProjectorGateway;
import com.getlipa.eventstore.core.projection.cdi.Projection;
import com.getlipa.eventstore.core.projection.cdi.Events;
import com.getlipa.eventstore.core.projection.trgt.middleware.Apply;
import io.quarkus.arc.deployment.*;
import io.quarkus.arc.processor.AnnotationsTransformer;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import org.jboss.jandex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

import static com.getlipa.eventstore.core.event.Event.EVENT_LOG_ID_NAMESPACE;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

class ProjectionProcessor {

    private static final Logger log = LoggerFactory.getLogger(ProjectionProcessor.class);

    @BuildStep
    @Record(STATIC_INIT)
    public void registerProjectionBeans(
            BeanDiscoveryFinishedBuildItem beanDiscovery,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
            ProjectorGateway.BeanRecorder gatewayRecorder,
            ProjectionMetadata.BeanRecorder metadataRecorder,
            ProjectionTargetFactory.BeanRecorder projectionTargetFactoryRecorder,
            ProjectionExtension.BeanRecorder extensionRecorder
    ) {
        beanDiscovery.beanStream()
                .withQualifier(com.getlipa.eventstore.core.projection.cdi.ProjectionTarget.Any.class)
                .stream()
                .forEach(bean -> {
                    final var name = bean.getQualifier(DotName.createSimple(Projection.Named.class))
                            .get()
                            .value()
                            .asString();

                    final var eventMatchers = bean.getQualifiers().stream()
                            .filter(qualifier -> qualifier.name().equals(DotName.createSimple(EventMatcher.class)))
                            .map(annotation -> SelectorFactory.valueOf(annotation.value(EventMatcher.FACTORY).asEnum())
                                    .create(annotation.value(EventMatcher.PARAMETER).asString()))
                            .toArray(Selector[]::new);
                    final var eventMatcher = Selector.all(eventMatchers);
                    final var aggregationStrategy = bean.getQualifier(DotName.createSimple(Events.Dispatch.class))
                            .map(annotation -> DispatchStrategy.valueOf(annotation.value().asEnum()))
                            .orElse(DispatchStrategy.defaultType());


                    final var metadata = new ProjectionMetadata(
                            name,
                            aggregationStrategy,
                            bean.getBeanClass().toString(),
                            eventMatcher
                    );

                    beanDiscovery.beanStream()
                            .withBeanType(ExtensionFactory.class)
                            .forEach(extensionFactoryBean -> {
                                syntheticBeans.produce(configure(ProjectionExtension.class, metadata)
                                        .named(String.format(
                                                "extension-%s-%s",
                                                extensionFactoryBean.getBeanClass().toString(),
                                                metadata.getName()
                                        ))
                                        .scope(ApplicationScoped.class)
                                        .forceApplicationClass()
                                        .createWith(extensionRecorder.record(metadata, extensionFactoryBean.getBeanClass().toString()))
                                        .done());
                            });


                    syntheticBeans.produce(createProjectionMetadata(metadata, metadataRecorder));
                    syntheticBeans.produce(createProjectorGateway(metadata, gatewayRecorder));
                    syntheticBeans.produce(createProjectionTargetFactory(bean, metadata, projectionTargetFactoryRecorder));
                });
    }

    private SyntheticBeanBuildItem createProjectionTargetFactory(
            final BeanInfo bean,
            final ProjectionMetadata metadata,
            final ProjectionTargetFactory.BeanRecorder recorder
    ) {
        final var middlewares = bean.getQualifiers()
                .stream()
                .filter(instance -> instance.name().equals((DotName.createSimple(Apply.class))))
                .map(instance -> instance.value().asClass().name().toString())
                .toArray(String[]::new);
        return configure(ProjectionTargetFactory.class, metadata)
                .scope(ApplicationScoped.class)
                .forceApplicationClass()
                .createWith(recorder.record(metadata, middlewares))
                .done();
    }

    SyntheticBeanBuildItem.ExtendedBeanConfigurator configure(
            final Class<?> implClazz,
            final ProjectionMetadata metadata
    ) {
        return SyntheticBeanBuildItem.configure(implClazz)
                .scope(Dependent.class)
                .addQualifier(
                        AnnotationInstance.builder(Projection.Any.class).build()
                )
                .addQualifier(
                        AnnotationInstance.builder(Projection.Named.class)
                                .value(metadata.getName())
                                .build()
                )
                .addQualifier(
                        AnnotationInstance.builder(Projection.Named.class)
                                .value(UuidGenerator.INSTANCE.generate(EVENT_LOG_ID_NAMESPACE, metadata.getName()).toString())
                                .build()
                )
                .addQualifier(
                        AnnotationInstance.builder(Projection.Named.class)
                                .value(metadata.getTargetClass())
                                .build()
                )
                .addQualifier(
                        AnnotationInstance.builder(com.getlipa.eventstore.core.projection.cdi.ProjectionTarget.Typed.class)
                                .value(metadata.getTargetClass())
                                .build()
                );
    }

    private SyntheticBeanBuildItem createProjectionMetadata(
            final ProjectionMetadata metadata,
            final ProjectionMetadata.BeanRecorder recorder
    ) {
        return configure(ProjectionMetadata.class, metadata)
                .scope(ApplicationScoped.class)
                .named(String.format("projection-metadata:%s", metadata.getName()))
                .forceApplicationClass()
                .createWith(recorder.record(metadata))
                .done();
    }

    private SyntheticBeanBuildItem createProjectorGateway(
            final ProjectionMetadata metadata,
            final ProjectorGateway.BeanRecorder recorder
    ) {
        return configure(ProjectorGateway.class, metadata)
                .scope(ApplicationScoped.class)
                .named(String.format("projector-gateway:%s", metadata.getName()))
                .forceApplicationClass()
                .createWith(recorder.record(metadata))
                .done();
    }

    @BuildStep
    AnnotationsTransformerBuildItem addLogIdMatcherAnnotation() {
        return addEventMatcherAnnotation(Events.WithLogId.class, SelectorFactory.WITH_LOG_ID);
    }

    @BuildStep
    AnnotationsTransformerBuildItem addCorrelationIdMatcherAnnotation() {
        return addEventMatcherAnnotation(Events.WithCorrelationId.class, SelectorFactory.WITH_CORRELATION_ID);
    }

    @BuildStep
    AnnotationsTransformerBuildItem addCausationIdMatcherAnnotation() {
        return addEventMatcherAnnotation(Events.WithCausationId.class, SelectorFactory.WITH_CAUSATION_ID);
    }

    @BuildStep
    AnnotationsTransformerBuildItem addLogDomainMatcherAnnotation() {
        return addEventMatcherAnnotation(Events.WithLogDomain.class, SelectorFactory.WITH_LOG_DOMAIN);
    }

    @BuildStep
    AnnotationsTransformerBuildItem addTypeMatcherAnnotation() {
        return addEventMatcherAnnotation(Events.WithType.class, SelectorFactory.WITH_TYPE);
    }

    AnnotationsTransformerBuildItem addEventMatcherAnnotation(
            final Class<? extends Annotation> annotation,
            final SelectorFactory selectorFactory
    ) {
        return new AnnotationsTransformerBuildItem(AnnotationsTransformer.appliedToClass()
                .whenClass(c -> c.hasAnnotation(annotation))
                .transform(c -> {
                            final var parameter = c.getTarget()
                                    .annotation(annotation)
                                    .value()
                                    .asString();
                            c.transform()
                                    .add(
                                            EventMatcher.class,
                                            AnnotationValue.createEnumValue("factory", DotName.createSimple(SelectorFactory.class), selectorFactory.name()),
                                            AnnotationValue.createStringValue("parameter", parameter)
                                    )
                                    .done();
                        }
                ));
    }


    @BuildStep
    AnnotationsTransformerBuildItem addProjectionAnnotations(BeanArchiveIndexBuildItem beanArchiveIndexBuildItem) {
        return new AnnotationsTransformerBuildItem(AnnotationsTransformer.appliedToClass()
                .whenClass(c -> c.hasAnnotation(Projection.class))
                .transform(c -> {
                            final var annotation = c.getTarget().annotation(Projection.class);
                            final var name = annotation.value("name").asString();
                            log.info(
                                    "Detected @{} bean: {} / {}",
                                    Projection.class.getSimpleName(),
                                    c.getTarget().toString(),
                                    name
                            );

                            c.transform()
                                    .add(com.getlipa.eventstore.core.projection.cdi.ProjectionTarget.Any.class)
                                    .add(
                                            Projection.Named.class,
                                            AnnotationValue.createStringValue("value", name)
                                    )
                                    .done();
                        }
                ));
    }
}
