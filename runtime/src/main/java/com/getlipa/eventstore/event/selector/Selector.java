package com.getlipa.eventstore.event.selector;

import com.getlipa.eventstore.event.AnyEvent;

public interface Selector {

    boolean matches(AnyEvent event);

    void accept(Visitor visitor);

    static Selector all(Selector... matchers) {
        return new CompositeSelector(matchers);
    }

    default Selector combineWith(Selector selector) {
        return all(this, selector);
    }

    static String toString(String type, String value) {
        return String.format("%s(%s)", type, value);
    }

    interface Visitor {

        default void visit(AllSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByCausationIdSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByCorrelationIdSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByLogIdSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByContextSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByLogSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(ByTypeSelector events) {
            throw new UnsupportedOperationException();
        }

        default void visit(CompositeSelector events) {
            throw new UnsupportedOperationException();
        }
    }

    interface IgnorantVisitor extends Visitor {
        default void visit(AllSelector events) {}

        default void visit(ByCausationIdSelector events) {}

        default void visit(ByCorrelationIdSelector events) {}

        default void visit(ByLogIdSelector events) {}

        default void visit(ByContextSelector events) {}

        default void visit(ByLogSelector events) {}

        default void visit(ByTypeSelector events) {}

        default void visit(CompositeSelector events) {}
    }
}
