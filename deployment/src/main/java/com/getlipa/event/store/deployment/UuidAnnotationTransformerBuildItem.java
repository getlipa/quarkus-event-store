package com.getlipa.event.store.deployment;

import io.quarkus.arc.deployment.AnnotationsTransformerBuildItem;
import io.quarkus.arc.processor.AnnotationsTransformer;
import org.jboss.jandex.AnnotationValue;

import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UuidAnnotationTransformerBuildItem {

    public static AnnotationsTransformerBuildItem create(
            final Class<? extends Annotation> stringAnnotation,
            final Class<? extends Annotation> uuidAnnotation
    ) {
        return new AnnotationsTransformerBuildItem(AnnotationsTransformer.appliedToClass()
                .whenClass(c -> c.hasAnnotation(stringAnnotation))
                .transform(c -> {
                            final var eventType = c.getTarget()
                                    .annotation(stringAnnotation)
                                    .value()
                                    .asString();
                            c.transform()
                                    .add(uuidAnnotation, toUUIDAnnotationValue(eventType))
                                    .done();
                        }
                ));
    }

    static AnnotationValue toUUIDAnnotationValue(String string) {
        final var uuid = UUID.nameUUIDFromBytes(string.getBytes(StandardCharsets.UTF_8)).toString();
        return AnnotationValue.createStringValue("uuid", uuid);
    }
}
