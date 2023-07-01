package com.getlipa.eventstore.core;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;

import java.util.Optional;

public class CdiUtil {

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
