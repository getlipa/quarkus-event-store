package com.getlipa.eventstore.core.event.selector;

import com.getlipa.eventstore.core.event.AnyEvent;
import com.getlipa.eventstore.core.event.Events;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class ByLogSelector implements Selector {

    private final String logDomain;

    private final UUID logId;

    @Override
    public boolean matches(AnyEvent event) {
        return logDomain.equals(event.getLogDomain()) && logId.equals(event.getLogId());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static ByLogSelector from(Selector ...selectors) {
        return from(Selector.all(selectors));
    }

    public static ByLogSelector from(Selector selector) {
        final var extractor = new Extractor();
        selector.accept(extractor);
        return extractor.extract();
    }

    @NoArgsConstructor
    private static class Extractor implements Selector.IgnorantVisitor {

        private ByLogIdSelector byLogIdSelector;

        private ByLogDomainSelector byLogDomainSelector;

        @Override
        public void visit(ByLogIdSelector byLogIdSelector) {
            this.byLogIdSelector = byLogIdSelector;
        }

        @Override
        public void visit(ByLogDomainSelector byLogDomainSelector) {
            this.byLogDomainSelector = byLogDomainSelector;
        }

        @Override
        public void visit(CompositeSelector events) {
            events.eachAccept(this);
        }

        ByLogSelector extract() {
            if (byLogDomainSelector == null || byLogIdSelector == null) {
                return null;
            }
            return Events.byLog(byLogDomainSelector.getLogDomain(), byLogIdSelector.getLogId());
        }
    }
}
