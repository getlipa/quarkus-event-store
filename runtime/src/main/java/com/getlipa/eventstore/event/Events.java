package com.getlipa.eventstore.event;

import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.event.selector.*;


public class Events {

    public static AllSelector all() {
        return new AllSelector();
    }

    public static ByContextSelector byContext(String context) {
        return new ByContextSelector(context);
    }

    public static ByCorrelationIdSelector byCorrelationId(Id correlationId) {
        return new ByCorrelationIdSelector(correlationId);
    }

    public static ByLogSelector byLog(String context, Id logId) {
        return new ByLogSelector(context, logId);
    }

    public static ByLogIdSelector byLogId(Id logId) {
        return new ByLogIdSelector(logId);
    }

    public static ByLogIdSelector byLogId(String logId) {
        return new ByLogIdSelector(logId);
    }
}
