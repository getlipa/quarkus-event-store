package com.getlipa.eventstore.aggregate.hydration;

import jakarta.enterprise.context.spi.CreationalContext;

public class AggregateCreationalContext<T> implements CreationalContext<T> {
    @Override
    public void push(T incompleteInstance) {

    }

    @Override
    public void release() {

    }
}
