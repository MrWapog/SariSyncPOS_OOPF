package sarisync.bootstrap;

import sarisync.services.ProductService;

/**
 * Seeds the default sari-sari store catalogue with fallback emoji icons.
 */
public final class CatalogSeeder {

    private CatalogSeeder() {}

    public static void seed(ProductService productService) {
        if (!productService.findAll().isEmpty()) return;

        productService.addProduct("Nova (BBQ)",          "Snacks",        25, 120, "", "🍿", "Crispy potato chips");
        productService.addProduct("Coca-Cola 1.5L",      "Beverages",     65,  80, "", "🥤", "Ice-cold cola");
        productService.addProduct("Pancit Canton",       "Instant Food",  15, 200, "", "🍜", "Instant noodles");
        productService.addProduct("Assorted Candy",      "Snacks",         5, 300, "", "🍬", "Sweet colorful candies");
        productService.addProduct("Shampoo Sachets",     "Personal Care",  8, 150, "", "🧴", "Single-use shampoo sachet");
        productService.addProduct("Canned Sardines",     "Canned Goods",  35, 100, "", "🐟", "Sardines in tomato sauce");
        productService.addProduct("Bottled Water 500ml", "Beverages",     12, 250, "", "💧", "Purified drinking water");
        productService.addProduct("Biscuits",            "Snacks",        20,  90, "", "🍪", "Crunchy butter biscuits");
    }
}
