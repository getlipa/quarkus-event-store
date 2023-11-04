package com.getlipa.eventstore.core.projection;

import com.getlipa.eventstore.core.event.selector.Selector;
import com.getlipa.eventstore.core.projection.projector.DispatchStrategy;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.annotations.RecordableConstructor;
import io.quarkus.runtime.annotations.Recorder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor(onConstructor = @__(@RecordableConstructor))
public class ProjectionMetadata {

    private final String name;

    private final DispatchStrategy dispatchStrategy;

    private final String targetClass; // FIXME

    private final Selector selector;

    public <T> T get(Class<T> configClass) {
        return (T) new SubscriptionConfig(); // FIXME
    }

    @Recorder
    public static class BeanRecorder {

        public Function<SyntheticCreationalContext<Object>, Object> record(final ProjectionMetadata metadata) {
            return context -> metadata;
        }
    }
}
