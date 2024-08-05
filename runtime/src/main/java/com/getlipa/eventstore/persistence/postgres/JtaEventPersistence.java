package com.getlipa.eventstore.persistence.postgres;

import com.getlipa.eventstore.event.AnyEvent;
import com.getlipa.eventstore.event.EphemeralEvent;
import com.getlipa.eventstore.event.Event;
import com.getlipa.eventstore.event.logindex.LogIndex;
import com.getlipa.eventstore.persistence.EventPersistence;
import com.getlipa.eventstore.persistence.exception.EventAppendException;
import com.getlipa.eventstore.event.selector.ByLogSelector;
import com.getlipa.eventstore.persistence.exception.InvalidIndexException;
import com.google.protobuf.Message;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;

@Slf4j
public abstract class JtaEventPersistence<T> implements EventPersistence {

    private static final String[] NOISY_LOGGERS_ON_INSERT_CONFLICT = new String[]{
            SqlExceptionHelper.class.getName(),
            "com.arjuna.ats.arjuna"
    };

    @Inject
    TransactionManager transactionManager;

    @Inject
    Vertx vertx;

    @Override
    public <P extends Message> Future<AnyEvent> append(ByLogSelector selector, LogIndex logIndex, EphemeralEvent<P> event) {
        return vertx.executeBlocking(result -> {
            try {
                appendBlocking(selector, logIndex, event);
            } catch (EventAppendException e) {
                result.fail(e);
                return;
            }
            read(event.getId())
                    .onSuccess(persisted -> result.complete(Event.from(persisted).withPayload(event.getPayload())))
                    .onFailure(result::fail);
        });
    }

    <P extends Message> void appendBlocking(ByLogSelector selector, LogIndex logIndex, EphemeralEvent<P> event) throws EventAppendException {
        try {
            final var suspendedTransaction = beginTransaction();
            doAppend(selector, logIndex, event);
            commitTransaction(suspendedTransaction);
        } catch (InvalidIndexException e) {
            throw InvalidIndexException.nonConsecutiveIndex(logIndex); // FIXME: Differentiate between non-consecutive & already defined index!!
        } catch (EventAppendException e) {
            throw e;
        } catch (Throwable e) {
            log.info("Unable to append event '{}': {}", e, e.getMessage());
            rollbackTransaction();
            throw EventAppendException.from(e);
        }
    }

    protected abstract void handleRollback(RollbackException rollbackException) throws EventAppendException;

    protected abstract <P extends Message> void doAppend(ByLogSelector selector, LogIndex logIndex, EphemeralEvent<P> event) throws EventAppendException;

    private Transaction beginTransaction() throws SystemException, NotSupportedException {
        Transaction currentTransaction = null;
        if (transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
            currentTransaction = transactionManager.suspend();
        }
        transactionManager.begin();
        return currentTransaction;
    }

    private void commitTransaction() throws SystemException, EventAppendException {
        final var mutedLogger = LoggerUtil.mute(NOISY_LOGGERS_ON_INSERT_CONFLICT);
        try {
            transactionManager.commit();
        } catch (RollbackException e) {
            handleRollback(e);
            log.error("Transaction commit failed and rolled back: " + e.getMessage());
        } catch (HeuristicMixedException e) {
            log.warn(
                    "Some transitions have been persisted and others have been rolled back due to a heuristic decision."
            );
        } catch (HeuristicRollbackException e) {
            log.warn(
                    "All transitions have been rolled back due to a heuristic decision."
            );
        } finally {
            mutedLogger.restore();
        }
    }

    private void commitTransaction(Transaction suspendedTransaction) throws SystemException, EventAppendException {
        commitTransaction();
        if (suspendedTransaction != null) {
            try {
                transactionManager.resume(suspendedTransaction);
            } catch (InvalidTransactionException e) {
                throw new IllegalStateException("Unable to resume previous transaction: " + e.getMessage());
            }
        }
    }


    private void rollbackTransaction() {
        try {
            transactionManager.rollback();
        } catch (SystemException e) {
            log.error("Unable to rollback transaction: " + e.getMessage());
        }
    }
}
