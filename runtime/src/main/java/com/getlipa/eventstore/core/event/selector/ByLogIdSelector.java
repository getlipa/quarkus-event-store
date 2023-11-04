package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.event.AnyEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByLogIdSelector implements Selector {

    private final String logId;

    @Override
    public boolean matches(AnyEvent event) {
        // TODO: make this.logId UUID?
        return logId.equals(event.getLogId().toString());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
