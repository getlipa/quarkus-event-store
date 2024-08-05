package com.getlipa.event.store.it;

import com.getlipa.eventstore.aggregate.hydration.ReHydrateMiddleware;
import com.getlipa.eventstore.aggregate.middleware.Use;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.hydration.eventhandler.Apply;
import com.getlipa.eventstore.it.BankAccount;
import com.getlipa.eventstore.projection.cdi.Projection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter
@Use(ReHydrateMiddleware.class)
@Projection(name = "client-wealth")
public class TotalBalance {

    private double totalBalance;

    @Apply
    public void onFundsDeposited(Event<BankAccount.FundsDeposited> event) {
        totalBalance += event.get().getAmount();
        log.info("Funds deposited: {} - new total balance: {}", event.get().getAmount(), totalBalance);
    }
}



