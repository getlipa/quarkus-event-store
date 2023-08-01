package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.actor.cdi.Actor;
import com.getlipa.eventstore.core.actor.messaging.Msg;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.example.event.Example;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Actor("demo")
public class DemoActor {

    private int count = 0;

    public Example.Simple doSomething(Msg<Example.Simple> simpleMsg) {
        log.error("Simple Msg received: {}", simpleMsg);
        return Example.Simple.newBuilder().setData("replyyyy " + count++).build();
    }

    public Example.Simple handleEvent(Event<Example.Simple> event) {
        log.error("EVENT received: {}", event);
        return Example.Simple.newBuilder().setData("replyyyy").build();

    }

    public String doFail(Msg<Example.Other> simpleMsg) {
        return "HAHA";
    }

}
