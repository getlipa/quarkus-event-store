package com.getlipa.eventstore.core.persistence;

import com.getlipa.eventstore.core.event.EphemeralEvent;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.stream.selector.ByCausationIdSelector;
import com.getlipa.eventstore.core.stream.selector.ByCorrelationIdSelector;
import com.getlipa.eventstore.core.stream.selector.BySeriesTypeSelector;
import com.getlipa.eventstore.core.stream.selector.ByTypeSelector;
import com.getlipa.eventstore.core.event.seriesindex.SeriesIndex;
import com.getlipa.eventstore.core.stream.options.ReadOptions;
import com.getlipa.eventstore.core.stream.selector.ByStreamSelector;
import com.google.protobuf.Message;

import java.util.Iterator;

public interface EventPersistence {


    <T extends Message> Event<T> append(ByStreamSelector selector, SeriesIndex seriesIndex, EphemeralEvent<T> event) throws EventAppendException;

    Iterator<Event<Message>> read(ByStreamSelector selector, ReadOptions readOptions);
    
    Iterator<Event<Message>> read(BySeriesTypeSelector selector, ReadOptions readOptions);
    
    Iterator<Event<Message>> read(ByCausationIdSelector selector, ReadOptions readOptions);
    
    Iterator<Event<Message>> read(ByCorrelationIdSelector selector, ReadOptions readOptions);
    
    Iterator<Event<Message>> read(ByTypeSelector selector, ReadOptions readOptions);
}
