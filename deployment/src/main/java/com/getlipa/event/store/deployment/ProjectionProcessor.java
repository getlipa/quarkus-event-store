package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.aggregate.cdi.AggregateBean;
import com.getlipa.eventstore.aggregate.cdi.AggregateCompanion;
import com.getlipa.eventstore.aggregate.cdi.AggregateContext;
import com.getlipa.eventstore.aggregate.cdi.AggregateType;
import com.getlipa.eventstore.aggregate.context.Context;
import com.getlipa.eventstore.projection.cdi.Projection;
import com.getlipa.eventstore.projection.cdi.ProjectionBean;
import com.getlipa.eventstore.projection.cdi.ProjectionCompanion;
import com.getlipa.eventstore.projection.cdi.ProjectionName;
import com.getlipa.eventstore.projection.extension.ExtensionFactory;
import com.getlipa.eventstore.projection.extension.ProjectionExtension;
import com.getlipa.eventstore.aggregate.hydration.AggregateHydratorFactory;
import com.getlipa.eventstore.projection.ProjectionMetadata;
import com.getlipa.eventstore.projection.projector.Projector;
import com.getlipa.eventstore.aggregate.middleware.Use;
import com.getlipa.eventstore.projection.projector.scope.ProjectorScoped;
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

import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

class ProjectionProcessor {

    private static final Logger log = LoggerFactory.getLogger(ProjectionProcessor.class);

    @BuildStep
    @Record(STATIC_INIT)
    public void registerProjectionCompanions(
            BeanDiscoveryFinishedBuildItem beanDiscovery,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans,
            Projector.BeanRecorder gatewayRecorder,
            ProjectionMetadata.BeanRecorder metadataRecorder,
            AggregateHydratorFactory.BeanRecorder projectionTargetFactoryRecorder,
            ProjectionExtension.BeanRecorder extensionRecorder
    ) {
        beanDiscovery.beanStream()
                .withQualifier(ProjectionBean.class)
                .stream()
                .forEach(bean -> {
                    final var context = Context.from(bean.getQualifier(DotName.createSimple(AggregateContext.class))
                            .get()
                            .value()
                            .asString());
                    final var name = bean.getQualifier(DotName.createSimple(ProjectionName.class))
                            .get()
                            .value()
                            .asString();
                    final var metadata = new ProjectionMetadata(
                            name,
                            context
                    );

                    final var buildInfo = new ProjectionBuildInfo(
                            metadata,
                            bean.getBeanClass().toString()
                    );

                    beanDiscovery.beanStream()
                            .withBeanType(ExtensionFactory.class)
                            .forEach(extensionFactoryBean -> syntheticBeans.produce(
                                    configure(ProjectionExtension.class, buildInfo)
                                            .named(String.format(
                                                    "extension-%s-%s",
                                                    extensionFactoryBean.getBeanClass().toString(),
                                                    metadata.getName()
                                            ))
                                            .scope(ApplicationScoped.class)
                                            .forceApplicationClass()
                                            .createWith(extensionRecorder.record(
                                                    metadata,
                                                    extensionFactoryBean.getBeanClass().toString()
                                            ))
                                            .done()));
                    syntheticBeans.produce(createProjectionMetadata(buildInfo, metadataRecorder));
                    syntheticBeans.produce(createAggregateContext(buildInfo, metadataRecorder));
                    syntheticBeans.produce(createProjectorGateway(buildInfo, gatewayRecorder));
                    syntheticBeans.produce(createProjectionTargetFactory(bean, buildInfo, projectionTargetFactoryRecorder));
                });
    }

    private SyntheticBeanBuildItem createProjectionTargetFactory(
            final BeanInfo bean,
            final ProjectionBuildInfo metadata,
            final AggregateHydratorFactory.BeanRecorder recorder
    ) {
        final var middlewares = bean.getQualifiers()
                .stream()
                .filter(instance -> instance.name().equals((DotName.createSimple(Use.class))))
                .map(instance -> instance.value().asClass().name().toString())
                .toArray(String[]::new);
        return configure(AggregateHydratorFactory.class, metadata)
                .scope(ApplicationScoped.class)
                .forceApplicationClass()
                .createWith(recorder.record(metadata.getMetadata().getContext(), metadata.getTypeName(), middlewares))
                .done();
    }

    SyntheticBeanBuildItem.ExtendedBeanConfigurator configure(
            final Class<?> implClazz,
            final ProjectionBuildInfo metadata
    ) {
        return SyntheticBeanBuildItem.configure(implClazz)
                .scope(Dependent.class)
                .addQualifier(AnnotationInstance.builder(ProjectionCompanion.class).build())
                .addQualifier(AnnotationInstance.builder(AggregateCompanion.class).build())
                .addQualifier(AnnotationInstance.builder(ProjectionName.class)
                        .value(metadata.getMetadata().getName())
                        .build()
                )
                .addQualifier(AnnotationInstance.builder(AggregateType.class)
                        .value(metadata.getTypeName())
                        .build()
                );
    }

    private SyntheticBeanBuildItem createProjectionMetadata(
            final ProjectionBuildInfo metadata,
            final ProjectionMetadata.BeanRecorder recorder
    ) {
        return configure(ProjectionMetadata.class, metadata)
                .scope(ApplicationScoped.class)
                .named(String.format("projection-metadata:%s", metadata.getMetadata().getName()))
                .forceApplicationClass()
                .createWith(recorder.record(metadata.getMetadata()))
                .done();
    }

    private SyntheticBeanBuildItem createAggregateContext(
            final ProjectionBuildInfo metadata,
            final ProjectionMetadata.BeanRecorder recorder
    ) {
        return configure(metadata.getMetadata().getContext().getClass(), metadata)
                .scope(ApplicationScoped.class)
                .named(String.format("aggregate-context:%s", metadata.getMetadata().getName()))
                .forceApplicationClass()
                .createWith(recorder.record(metadata.getMetadata().getContext()))
                .done();
    }

    private SyntheticBeanBuildItem createProjectorGateway(
            final ProjectionBuildInfo metadata,
            final Projector.BeanRecorder recorder
    ) {
        return configure(Projector.class, metadata)
                .scope(ApplicationScoped.class)
                .named(String.format("projector-gateway:%s", metadata.getMetadata().getName()))
                .forceApplicationClass()
                .createWith(recorder.record(metadata.getMetadata()))
                .done();
    }

    @BuildStep
    AnnotationsTransformerBuildItem addProjectionAnnotations(BeanArchiveIndexBuildItem beanArchiveIndexBuildItem) {
        return new AnnotationsTransformerBuildItem(AnnotationsTransformer.appliedToClass()
                .whenClass(c -> c.hasAnnotation(Projection.class))
                .transform(c -> {
                            final var annotation = c.getTarget().annotation(Projection.class);
                            final var aggregateType = c.getTarget().asClass().toString();
                            final var name = annotation.value("name").asString();
                            final var context = annotation.valueWithDefault(
                                    beanArchiveIndexBuildItem.getIndex(),
                                    "context"
                            ).asString();
                            log.info(
                                    "Detected @{} bean: {} / {}",
                                    Projection.class.getSimpleName(),
                                    aggregateType,
                                    name
                            );
                            c.transform()
                                    .add(ProjectionName.class, AnnotationValue.createStringValue("value", name))
                                    .add(Dependent.class)
                                    //.add(ProjectorScoped.class)
                                    .add(ProjectionBean.class)
                                    .add(AggregateBean.class)
                                    .add(AggregateType.class, AnnotationValue.createStringValue("value", aggregateType))
                                    .add(AggregateContext.class, AnnotationValue.createStringValue("value", context))
                                    .done();
                        }
                ));
    }
}
