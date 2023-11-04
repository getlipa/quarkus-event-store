package com.getlipa.eventstore.core.proto;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;

public interface AnyPayload {

    Message get();

    public static  interface Decoder<T, P extends GeneratedMessageV3> {

        T decode(P proto);

    }

    public static  interface Encoder<T, P extends GeneratedMessageV3> {

        P encode(T instance);

    }
}
