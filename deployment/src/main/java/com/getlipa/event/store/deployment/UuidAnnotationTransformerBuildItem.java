package com.getlipa.event.store.deployment;

import com.getlipa.eventstore.core.UuidGenerator;
import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import org.jboss.jandex.AnnotationValue;

import java.lang.annotation.Annotation;

public class UuidAnnotationTransformerBuildItem {

    private static final UuidGenerator uuidGenerator = new UuidGenerator();

    public static AnnotationsTransformerBuildItem create(
            final Class<? extends Annotation> stringAnnotation,
            final Class<? extends Annotation> uuidAnnotation,
            final String namespace
    ) {
        return new AnnotationsTransformerBuildItem(AnnotationsTransformer.appliedToClass()
                .whenClass(c -> c.hasAnnotation(stringAnnotation))
                .transform(c -> {
                            final var eventType = c.getTarget()
                                    .annotation(stringAnnotation)
                                    .value()
                                    .asString();
                            c.transform()
                                    .add(uuidAnnotation, toUUIDAnnotationValue(namespace, eventType))
                                    .done();
                        }
                ));
    }

    static AnnotationValue toUUIDAnnotationValue(final String namespace, final String string) {
        final String uuid = uuidGenerator.generate(namespace, string).toString();
        return AnnotationValue.createStringValue("uuid", uuid);
    }
}
