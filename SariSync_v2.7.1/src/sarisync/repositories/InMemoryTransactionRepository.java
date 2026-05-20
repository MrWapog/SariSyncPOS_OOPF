package sarisync.repositories;

import sarisync.models.Transaction;

import java.util.Optional;

/**
 * INHERITANCE — extends InMemoryTransactionRepositoryImpl
 */
public class InMemoryTransactionRepository extends InMemoryTransactionRepositoryImpl {

    @Override
    public Optional<Transaction> findLastCompleted() {
        return super.findLastCompleted();
    }

    public Optional<Transaction> findByNumber(String transactionNumber) {
        return findAll().stream()
            .filter(t -> t.getTransactionNumber().equals(transactionNumber))
            .findFirst();
    }
}
