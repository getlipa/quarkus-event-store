package com.getlipa.eventstore.persistence.postgres;

import com.getlipa.eventstore.event.selector.*;
import com.getlipa.eventstore.persistence.postgres.query.Sorting;
import com.getlipa.eventstore.persistence.postgres.query.StartAtVisitor;
import com.getlipa.eventstore.persistence.postgres.query.UntilVisitor;
import com.getlipa.eventstore.stream.reader.cursor.PositionCursor;
import com.getlipa.eventstore.stream.reader.ReadOptions;
import com.getlipa.eventstore.stream.reader.cursor.LogIndexCursor;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EventQuery {

    private static final Sort SORT_ASC = Sort.ascending("position");

    private static final Sort SORT_DESC = Sort.descending("position");
    private static final Logger log = LoggerFactory.getLogger(EventQuery.class);

    private final String query;

    private final Sort sort;

    private final int limit;

    private final Parameters parameters;

    public static EventQuery create(Selector selector, ReadOptions readOptions) {
        return Builder.create(selector, readOptions);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder implements StartAtVisitor, UntilVisitor, Selector.Visitor {

        private final StringBuilder query = new StringBuilder();

        private final Sorting direction;

        private Parameters parameters;

        static EventQuery create(Selector selector, ReadOptions readOptions) {
            final var factory = new Builder(Sorting.valueOf(readOptions.direction()));
            selector.accept(factory);
            return create(factory, readOptions);
        }

        @Override
        public void visit(AllSelector events) {
            log.trace("All selector does not require an additional condition.");
        }

        @Override
        public void visit(ByLogSelector selector) {
            addCondition("logDomainUuid = :logDomainUuid AND logUuid = :logUuid");
            addParameter("logDomainUuid", JpaEvent.createLogDomainId(selector.getContext()));
            addParameter("logUuid", selector.getLogId().toUuid());
        }

        @Override
        public void visit(CompositeSelector compositeSelector) {
            compositeSelector.eachAccept(this);
        }

        @Override
        public void visit(ByContextSelector selector) {
            addCondition("logDomainUuid = :logDomainUuid");
            addParameter("logDomainUuid", JpaEvent.createLogDomainId(selector.getLogDomain()));
        }

        @Override
        public void visit(ByLogIdSelector selector) {
            addCondition("logUuid = :logUuid");
            addParameter("logUuid", selector.getLogId().toUuid());
        }

        @Override
        public void visit(ByCorrelationIdSelector selector) {
            addCondition("correlationUuid = :correlationUuid");
            addParameter("correlationUuid", selector.getCorrelationUuid());
        }

        private void addCondition(final String condition) {
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append(condition);
        }

        private void addParameter(final String name, final Object value) {
            if (parameters == null) {
                parameters = Parameters.with(name, value);
                return;
            }
            parameters = parameters.and(name, value);
        }

        static EventQuery create(Builder builder, ReadOptions readOptions) {
            readOptions.from().accept((StartAtVisitor) builder);
            readOptions.until().accept((UntilVisitor) builder);
            return new EventQuery(
                    builder.query.toString(),
                    builder.direction.getSort(),
                    readOptions.limit(),
                    builder.parameters
            );
        }

        @Override
        public void visitStartAt(LogIndexCursor cursor) {
            addCondition(String.format("logIndex %s :startAt", direction.getOperator()));
            addParameter("startAt", cursor.getLogIndex());
        }

        @Override
        public void visitStartAt(PositionCursor cursor) {
            addCondition(String.format("position %s :startAt", direction.getOperator()));
            addParameter("startAt", cursor.getPosition());
        }

        @Override
        public void visitUntil(LogIndexCursor cursor) {
            addCondition(String.format("position %s :until", direction.getOppositeOperator()));
            addParameter("until", cursor.getLogIndex());
        }

        @Override
        public void visitUntil(PositionCursor cursor) {
            addCondition(String.format("position %s :until", direction.getOppositeOperator()));
            addParameter("until", cursor.getPosition());
        }
    }
}
