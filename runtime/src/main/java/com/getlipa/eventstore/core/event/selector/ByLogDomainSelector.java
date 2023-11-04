package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.UuidGenerator;
import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Event;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByLogDomainSelector implements Selector {

    private final String logDomain;

    @Override
    public boolean matches(AnyEvent event) {
        return logDomain.equals(event.getLogDomain());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
