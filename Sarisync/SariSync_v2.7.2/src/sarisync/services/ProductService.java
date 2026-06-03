package sarisync.services;

import sarisync.models.Product;
import sarisync.repositories.InMemoryProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ENCAPSULATION + INHERITANCE 
 */
public class ProductService {

    // ── Dependency (Encapsulation) ────────────────────────────────────────
    private final InMemoryProductRepository repository;

    public ProductService(InMemoryProductRepository repository) {
        this.repository = repository;
    }

    // ── CRUD ──────────────────────────────────────────────────────────────

    /**
     * Adds a new product.
     * @throws IllegalArgumentException if a product with the same name already exists.
     */
    public Product addProduct(String name, String category, double price,
                              int stock, String imageUrl, String description) {
        boolean nameTaken = repository.findAll().stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(name));
        if (nameTaken) throw new IllegalArgumentException("A product named '" + name + "' already exists");

        Product product = new Product(name, category, price, stock, imageUrl, description);
        return repository.save(product);
    }

    /**
     * Updates an existing product's mutable fields.
     * @throws IllegalArgumentException if the product is not found.
     */
    public Product updateProduct(String id, String name, String category,
                                 double price, int stock,
                                 String imageUrl, String description) {
        Product product = repository.findById(id).orElseThrow(() ->
            new IllegalArgumentException("Product not found: " + id)
        );
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setStock(stock);
        product.setImageUrl(imageUrl);
        product.setDescription(description);
        return repository.update(product);
    }

    /**
     * Deletes a product by ID.
     * @throws IllegalArgumentException if the product is not found.
     */
    public void deleteProduct(String id) {
        if (!repository.deleteById(id))
            throw new IllegalArgumentException("Product not found: " + id);
    }

    // ── Queries ───────────────────────────────────────────────────────────

    public List<Product> findAll()              { return repository.findAll();            }
    public Optional<Product> findById(String id) { return repository.findById(id);        }

    public List<Product> findByCategory(String category) {
        return repository.findAll().stream()
            .filter(p -> p.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }

    public List<Product> search(String query) {
        String lower = query.toLowerCase();
        return repository.findAll().stream()
            .filter(p -> p.getName().toLowerCase().contains(lower)
                      || p.getCategory().toLowerCase().contains(lower))
            .collect(Collectors.toList());
    }

    /** Returns products with stock at or below the low-stock threshold. */
    public List<Product> findLowStock() {
        return repository.findAll().stream()
            .filter(Product::isLowStock)
            .collect(Collectors.toList());
    }

    /** Returns all distinct category names. */
    public List<String> getCategories() {
        return repository.findAll().stream()
            .map(Product::getCategory)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
