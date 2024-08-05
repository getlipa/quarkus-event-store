package com.getlipa.eventstore.projection;

import com.getlipa.eventstore.aggregate.context.Context;
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

    private final Context context;

    @Recorder
    public static class BeanRecorder {

        public Function<SyntheticCreationalContext<Object>, Object> record(final Object metadata) {
            return context -> metadata;
        }
    }
}
