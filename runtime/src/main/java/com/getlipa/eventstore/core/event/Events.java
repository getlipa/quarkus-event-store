package com.getlipa.eventstore.core.event;

import com.getlipa.eventstore.core.UuidGenerator;
import com.getlipa.eventstore.core.event.selector.*;
import com.getlipa.eventstore.core.proto.ProtoUtil;

import java.util.UUID;

public class Events {

    public static AllSelector all() {
        return new AllSelector();
    }

    public static ByLogDomainSelector byLogDomain(String logDomain) {
        return new ByLogDomainSelector(logDomain);
    }

    public static ByCorrelationIdSelector byCorrelationId(UUID correlationId) {
        return new ByCorrelationIdSelector(correlationId);
    }

    public static ByLogSelector byLog(String logDomain, String logId) {
        return byLog(logDomain, ProtoUtil.toUUID(Event.EVENT_LOG_ID_NAMESPACE, logId));
    }

    public static ByLogSelector byLog(String logDomain, UUID logId) {
        return new ByLogSelector(logDomain, logId);
    }

    public static ByLogSelector byLog(UUID logType, UUID logId) {
        return new ByLogSelector(logType.toString(), logId);
    }

    public static ByLogIdSelector byLogId(String logId) {
        return new ByLogIdSelector(logId);
    }

    public static ByLogIdSelector byLogId(UUID logId) {
        return byLogId(logId.toString());
    }
}
