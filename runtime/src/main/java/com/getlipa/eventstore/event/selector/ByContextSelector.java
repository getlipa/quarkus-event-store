package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByContextSelector implements Selector {

    private final String logDomain;

    @Override
    public boolean matches(AnyEvent event) {
        return logDomain.equals(event.getLogContext());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return Selector.toString("context", logDomain);
    }
}
