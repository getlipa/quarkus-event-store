package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.identifier.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByLogIdSelector implements Selector {

    private final Id logId;

    public ByLogIdSelector(String logId) {
        this(Id.derive(logId));
    }

    @Override
    public boolean matches(AnyEvent event) {
        return logId.equals(event.getLogId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }


    @Override
    public String toString() {
        return Selector.toString("logId", logId.toString());
    }
}
