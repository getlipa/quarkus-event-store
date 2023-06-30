package com.getlipa.eventstore.core.stream.selector;

import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.EventPersistence;
import com.getlipa.eventstore.core.proto.ProtoUtil;
import com.getlipa.eventstore.core.stream.options.ReadOptions;
import com.google.protobuf.Message;

import java.util.Iterator;
import java.util.UUID;

public class Events {

    public static BySeriesTypeSelector bySeriesType(String seriesType) {
        return new BySeriesTypeSelector(seriesType);
    }

    public static ByCorrelationIdSelector byCorrelationId(UUID correlationId) {
        return new ByCorrelationIdSelector(correlationId);
    }

    public static ByStreamSelector bySeries(String seriesType, String seriesId) {
        return bySeries(seriesType, ProtoUtil.toUUID(seriesId));
    }

    public static ByStreamSelector bySeries(String seriesType, UUID seriesId) {
        return new ByStreamSelector(seriesType, seriesId);
    }

    public static ByStreamSelector bySeries(UUID seriesType, UUID seriesId) {
        return new ByStreamSelector(seriesType.toString(), seriesId);
    }

    public static interface Selector {

        Iterator<Event<Message>> readFrom(EventPersistence eventPersistence, ReadOptions readOptions);
    }


}
