package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.identifier.Id;
import io.quarkus.runtime.annotations.RecordableConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@EqualsAndHashCode
public class ByCorrelationIdSelector implements Selector {

    private final Id correlationId;


    @RecordableConstructor
    public ByCorrelationIdSelector(String correlationId) {
        this(Id.from(UUID.fromString(correlationId)));
    }

    // Required for byte code recording
    public String getCorrelationId() {
        return correlationId.toUuid().toString();
    }

    public UUID getCorrelationUuid() {
        return correlationId.toUuid();
    }

    @Override
    public boolean matches(AnyEvent event) {
        return correlationId.equals(event.getCorrelationId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return Selector.toString("correlationId", correlationId.toString());
    }
}
