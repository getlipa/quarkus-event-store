package com.getlipa.eventstore.core.projection.trgt.middleware.stateful;

public interface Snapshottable<T> {

    void loadSnapshot(T snapshot);

}
