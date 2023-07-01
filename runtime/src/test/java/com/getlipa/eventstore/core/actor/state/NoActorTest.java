package com.getlipa.eventstore.core.actor.state;

import com.getlipa.eventstore.core.actor.cdi.ActorId;
import com.getlipa.eventstore.core.actor.messaging.MessageDelivery;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.SharedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

@ExtendWith(MockitoExtension.class)
class NoActorTest {

    private static final long INITIAL_COUNTER_VALUE = 0;

    private static final ActorId ACTOR_ID = ActorId.createDefault("actor");
    private static final String VERTICLE_ID = "actor:" + ACTOR_ID;

    private static final String DEPLOYMENT_ID = "deployment";

    @Mock
    private Vertx vertx;

    @Mock
    private Counter counter;

    @Mock
    private MessageDelivery messageDelivery;

    private NoActor noActor;

    @BeforeEach
    public void setup() {
        noActor = new NoActor(vertx);
        Mockito.doReturn(ACTOR_ID).when(messageDelivery).getActorId();
    }

    @Test
    public void shouldDeploy() {
        Mockito.doReturn(Future.succeededFuture(DEPLOYMENT_ID)).when(vertx).deployVerticle(VERTICLE_ID);

        noActor.process(messageDelivery);

        Mockito.verify(vertx).deployVerticle(VERTICLE_ID);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DeploymentSuccessfulTest {

        @BeforeEach
        public void setup() {
            final var sharedData = Mockito.mock(SharedData.class);
            Mockito.doReturn(sharedData).when(vertx).sharedData();
            Mockito.doReturn(Future.succeededFuture(counter)).when(sharedData).getCounter(ACTOR_ID.toString());
            Mockito.doReturn(Future.succeededFuture(INITIAL_COUNTER_VALUE)).when(counter).get();
            Mockito.doReturn(Future.succeededFuture()).when(messageDelivery).deliver(vertx);
            Mockito.doReturn(Future.succeededFuture(DEPLOYMENT_ID)).when(vertx).deployVerticle(VERTICLE_ID);
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        class RegistrationSuccessfulTest {

            @BeforeEach
            public void setup() {
                Mockito.doReturn(Future.succeededFuture(true)).when(counter).compareAndSet(Mockito.anyLong(), Mockito.anyLong());

                noActor.process(messageDelivery);
            }

            @Test
            public void shouldRegisterActorInstance() {
                Mockito.verify(counter).compareAndSet(0, DEPLOYMENT_ID.hashCode());
            }

            @Test
            public void shouldDeliverMessage() {
                Mockito.verify(messageDelivery).deliver(vertx);
            }

            @Test
            public void shouldNotUndeploy() {
                Mockito.verify(vertx, Mockito.never()).undeploy(DEPLOYMENT_ID);
            }
        }

        @Nested
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        class RegistrationFailedTest {

            @BeforeEach
            public void setup() {
                Mockito.doReturn(Future.succeededFuture(false)).when(counter).compareAndSet(Mockito.anyLong(), Mockito.anyLong());
                Mockito.doReturn(Future.succeededFuture()).when(vertx).undeploy(DEPLOYMENT_ID);
                noActor.process(messageDelivery);
            }

            @Test
            public void shouldUndeploy() {
                Mockito.verify(vertx).undeploy(DEPLOYMENT_ID);
            }

            @Test
            public void shouldStillDeliverMessage() {
                Mockito.verify(messageDelivery).deliver(vertx);
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DeploymentFailedTest {

        @BeforeEach
        public void setup() {
            Mockito.doReturn(Future.failedFuture("failed")).when(vertx).deployVerticle(VERTICLE_ID);
            noActor.process(messageDelivery);
        }

        @Test
        public void shouldNotRegister() {
            Mockito.verify(counter, Mockito.never()).compareAndSet(Mockito.anyLong(), Mockito.anyLong());
        }

        @Test
        public void shouldNotDeliverMessage() {
            Mockito.verify(messageDelivery, Mockito.never()).deliver(vertx);
        }
    }
}