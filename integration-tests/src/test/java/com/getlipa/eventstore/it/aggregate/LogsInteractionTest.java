package com.getlipa.eventstore.it.aggregate;

import com.getlipa.eventstore.EventStore;
import com.getlipa.eventstore.aggregate.Logs;
import com.getlipa.eventstore.aggregate.hydration.ReHydrateMiddleware;
import com.getlipa.eventstore.aggregate.middleware.Use;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.event.Events;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.hydration.eventhandler.Apply;
import com.getlipa.eventstore.identifier.Id;
import com.getlipa.eventstore.it.BankAccount;
import com.getlipa.eventstore.projection.cdi.Projection;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.vertx.core.Future;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class LogsInteractionTest {

    @Inject
    Logs<AccountBalance> accountBalances;

    private static final Id NEW_CUSTOMER = Id.numeric(1);

    private static final Id REFERRING_CUSTOMER = Id.numeric(2);

    @Test
    public void testStateReHydration() {
        final var referringCustomerAccount = accountBalances.get(REFERRING_CUSTOMER);
        referringCustomerAccount.append(LogIndex.atAny())
                .withPayload(BankAccount.AccountOpened.newBuilder().build())
                .toCompletionStage().toCompletableFuture().join();

        final var newCustomerAccount = accountBalances.get(NEW_CUSTOMER);
        newCustomerAccount.append()
                .withCausationId(REFERRING_CUSTOMER)
                .withPayload(BankAccount.AccountOpened.newBuilder().build())
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        assertEquals(
                AccountBalance.WELCOME_AMOUNT,
                newCustomerAccount.get(true).toCompletionStage().toCompletableFuture().join().balance
        );
        assertEquals(
                AccountBalance.WELCOME_AMOUNT + AccountBalance.REFERRAL_REWARD,
                referringCustomerAccount.get(true).toCompletionStage().toCompletableFuture().join().balance
        );
    }

    @Projection(name = "account-balance-complex", context = "account-balance")
    @Use(ReHydrateMiddleware.class)
    public static class AccountBalance {

        static final double WELCOME_AMOUNT = 10;

        static final double REFERRAL_REWARD = 15;

        @Inject
        Logs<AccountBalance> logs;

        @Inject
        EventStore eventStore;

        double balance = 0;

        @Apply
        public Future<Void> onAccountOpened(Event<BankAccount.AccountOpened> event) {
            final var welcomeDeposit = eventStore.stream(Events.byLog(event.getLogContext(), event.getLogId()))
                    .append(LogIndex.atAny())
                    .withId(Id.derive("welcome-deposit", event.getId()))
                    .withPayload(BankAccount.FundsDeposited.newBuilder().setAmount(WELCOME_AMOUNT).build())
                    .mapEmpty();
            final var referrerDeposit = logs.get(event.getCausationId())
                    .append(LogIndex.atAny())
                    .withId(Id.derive("referral-reward", event.getId()))
                    .withPayload(BankAccount.FundsDeposited.newBuilder().setAmount(REFERRAL_REWARD).build())
                    .mapEmpty();
            return Future.all(welcomeDeposit, referrerDeposit).mapEmpty();
        }

        @Apply
        public void onFundsDeposited(Event<BankAccount.FundsDeposited> event) {
            balance += event.get().getAmount();
        }
    }
}
