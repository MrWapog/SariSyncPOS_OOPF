package sarisync.repositories;

import sarisync.interfaces.IRepository;
import sarisync.models.Customer;

import java.util.*;

/**
 * INHERITANCE
 */
public class InMemoryCustomerRepository implements IRepository<Customer, String> {

    private final Map<String, Customer> store = new LinkedHashMap<>();

    @Override
    public Customer save(Customer customer) {
        store.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public Optional<Customer> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Customer> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Customer update(Customer customer) {
        if (!store.containsKey(customer.getId()))
            throw new NoSuchElementException("Customer not found: " + customer.getId());
        store.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    @Override
    public int count() { return store.size(); }
}
