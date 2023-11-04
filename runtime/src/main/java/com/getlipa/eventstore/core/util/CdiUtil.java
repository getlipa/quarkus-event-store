package com.getlipa.eventstore.core.util;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class CdiUtil {

    public static <T extends Annotation> Optional<T> annotation(Class<T> annotation, Instance<?> instance) {
        return annotation(annotation, instance.getHandle());
    }

    public static <T extends Annotation> Optional<T> annotation(Class<T> annotation, Instance.Handle<?> instanceHandle) {
        return annotation(annotation, instanceHandle.getBean());
    }

    public static <T extends Annotation> Optional<T> annotation(Class<T> annotation, Bean<?> bean) {
        return Optional.ofNullable(bean.getBeanClass().getAnnotation(annotation));
    }

    public static <T> Optional<T> qualifier(Class<T> annotation, Instance<?> instance) {
        return qualifier(annotation, instance.getHandle());
    }

    public static <T> Optional<T> qualifier(Class<T> annotation, Instance.Handle<?> instanceHandle) {
        return qualifier(annotation, instanceHandle.getBean());
    }

    public static <T> Optional<T> qualifier(Class<T> annotation, Bean<?> bean) {
        return bean.getQualifiers().stream()
                .filter(annotation::isInstance)
                .map(annotation::cast)
                .findFirst();
    }

}
