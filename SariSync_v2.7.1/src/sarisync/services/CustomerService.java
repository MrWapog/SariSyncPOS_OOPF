package sarisync.services;

import sarisync.models.Customer;
import sarisync.repositories.InMemoryCustomerRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ENCAPSULATION + ABSTRACTION
 
 */
public class CustomerService {

    private final InMemoryCustomerRepository repository;

    public CustomerService(InMemoryCustomerRepository repository) {
        this.repository = repository;
    }

    // ── CRUD 

    /**
     * Registers a new customer.
     * @throws IllegalArgumentException on duplicate name or contact number.
     */
    public Customer addCustomer(String name, String contactNumber) {
        validateUnique(null, name, contactNumber);
        Customer c = new Customer(name.trim(), contactNumber.trim());
        return repository.save(c);
    }

    /**
     * Updates an existing customer's name and/or contact number.
     */
    public Customer updateCustomer(String id, String name, String contactNumber) {
        Customer c = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        validateUnique(id, name, contactNumber);
        c.setName(name.trim());
        c.setContactNumber(contactNumber.trim());
        return repository.update(c);
    }

    /**
     * Deletes a customer by ID.
     */
    public void deleteCustomer(String id) {
        if (!repository.deleteById(id))
            throw new IllegalArgumentException("Customer not found: " + id);
    }

    // ── Queries 

    public List<Customer> findAll() { return repository.findAll(); }

    public Optional<Customer> findById(String id) { return repository.findById(id); }

    /**
     * Search customers by name or contact number (case-insensitive).
     * Used in POS customer selector.
     */
    public List<Customer> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String lower = query.toLowerCase().trim();
        return repository.findAll().stream()
            .filter(c -> c.getName().toLowerCase().contains(lower)
                      || c.getContactNumber().contains(lower))
            .collect(Collectors.toList());
    }

    public Optional<Customer> findByContactNumber(String contact) {
        return repository.findAll().stream()
            .filter(c -> c.getContactNumber().equals(contact.trim()))
            .findFirst();
    }

    // ── Validation 

    private void validateUnique(String excludeId, String name, String contactNumber) {
        boolean nameTaken = repository.findAll().stream()
            .filter(c -> excludeId == null || !c.getId().equals(excludeId))
            .anyMatch(c -> c.getName().equalsIgnoreCase(name.trim()));
        if (nameTaken)
            throw new IllegalArgumentException("A customer named '" + name + "' already exists.");

        boolean phoneTaken = repository.findAll().stream()
            .filter(c -> excludeId == null || !c.getId().equals(excludeId))
            .anyMatch(c -> c.getContactNumber().equals(contactNumber.trim()));
        if (phoneTaken)
            throw new IllegalArgumentException("Contact number '" + contactNumber + "' is already registered.");
    }
}
