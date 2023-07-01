/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.getlipa.event.store.it;

import com.getlipa.eventstore.core.EventStore;
import com.getlipa.eventstore.core.actor.Gateway;
import com.getlipa.eventstore.core.actor.messaging.Command;
import com.getlipa.eventstore.core.event.Event;
import com.getlipa.eventstore.core.persistence.exception.EventAppendException;
import com.getlipa.eventstore.core.stream.options.Cursor;
import com.getlipa.eventstore.core.event.seriesindex.SeriesIndex;
import com.getlipa.eventstore.core.stream.selector.Events;
import com.getlipa.eventstore.example.event.Example;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Path("/event-store")
@ApplicationScoped
@RequiredArgsConstructor
public class EventStoreResource {
    // add some rest methods here

    private final Gateway<DemoActor> gateway;

    private final EventStore eventStore;

    @GET
    public String hello() {



        final var test = gateway.compute("gugus", actor -> actor.doSomething(Command.withPayload(Example.Simple.newBuilder().build())));

        final var test2 = gateway.compute("dada", actor -> actor.doSomething(Command.withPayload(Example.Simple.newBuilder().build())));

        return "Hello event-store - " + test;
    }

    @GET
    @Path("append")
    public String append() throws EventAppendException {
        return eventStore.stream(Events.bySeries("gugus", "gugus"))
                .append(SeriesIndex.atAny(), Event.withPayload(Example.Simple.newBuilder()
                                .setData("test")
                        .build()))
                .toString();
    }
}
