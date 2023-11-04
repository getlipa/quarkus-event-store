package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.event.AnyEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByCorrelationIdSelector implements Selector {

    private final UUID correlationId;

    public ByCorrelationIdSelector(String correlationId) {
        this(UUID.fromString(correlationId));
    }

    @Override
    public boolean matches(AnyEvent event) {
        return correlationId.equals(event.getCorrelationId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
