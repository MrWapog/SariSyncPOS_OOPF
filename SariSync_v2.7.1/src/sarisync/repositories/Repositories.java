package sarisync.repositories;

import sarisync.interfaces.IRepository;
import sarisync.models.Product;
import sarisync.models.Transaction;

import java.util.*;
import java.util.stream.Collectors;

// 
//  INHERITANCE — Both repositories implement IRepository.
//  The service layer depends only on the IRepository interface, so these
//  in-memory implementations can later be replaced with JPA/SQL versions
//  without touching a single line of service code.
// 

// ─── Product Repository 

class InMemoryProductRepositoryImpl implements IRepository<Product, String> {

    protected final Map<String, Product> store = new LinkedHashMap<>();

    @Override
    public Product save(Product product) {
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Product update(Product product) {
        if (!store.containsKey(product.getId()))
            throw new NoSuchElementException("Product not found: " + product.getId());
        store.put(product.getId(), product);
        return product;
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    @Override
    public int count() { return store.size(); }
}

// ─── Transaction Repository 

class InMemoryTransactionRepositoryImpl implements IRepository<Transaction, String> {

    protected final Map<String, Transaction> store = new LinkedHashMap<>();

    @Override
    public Transaction save(Transaction txn) {
        store.put(txn.getId(), txn);
        return txn;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Transaction update(Transaction txn) {
        if (!store.containsKey(txn.getId()))
            throw new NoSuchElementException("Transaction not found: " + txn.getId());
        store.put(txn.getId(), txn);
        return txn;
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    @Override
    public int count() { return store.size(); }

    /** Returns the most recently completed (non-voided) transaction. */
    public Optional<Transaction> findLastCompleted() {
        List<Transaction> all = new ArrayList<>(store.values());
        Collections.reverse(all);
        return all.stream().filter(Transaction::isCompleted).findFirst();
    }
}
