package com.getlipa.eventstore.aggregate.hydration.snapshot;

public interface Snapshottable<T> {

    void loadSnapshot(T snapshot);

}
