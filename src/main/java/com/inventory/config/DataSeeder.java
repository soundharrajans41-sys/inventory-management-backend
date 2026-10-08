package com.inventory.config;

import com.inventory.entity.*;
import com.inventory.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final AlertRepository alertRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      SupplierRepository supplierRepository,
                      ProductRepository productRepository,
                      StockMovementRepository stockMovementRepository,
                      SalesOrderRepository salesOrderRepository,
                      PurchaseOrderRepository purchaseOrderRepository,
                      AlertRepository alertRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.alertRepository = alertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking database seed status...");

        // 1. Seed Users
        seedUsers();

        // 2. Clean dummy and test products
        cleanupDummyTestProducts();

        // 3. Seed Categories & Suppliers
        seedCategoriesAndSuppliers();

        // 4. Seed Realistic Products
        seedProducts();

        // 5. Seed Orders & Alerts
        seedOrdersAndAlerts();

        log.info("Database seeding check complete.");
    }

    private void seedUsers() {
        if (!userRepository.existsByEmail("karthi@gmail.com")) {
            User admin = new User("Karthi", "karthi@gmail.com", passwordEncoder.encode("123"), Role.ADMIN);
            userRepository.save(admin);
            log.info("Seeded Admin user: karthi@gmail.com / 123");
        }
        if (!userRepository.existsByEmail("admin@inventory.com")) {
            User admin = new User("Administrator", "admin@inventory.com", passwordEncoder.encode("admin123"), Role.ADMIN);
            userRepository.save(admin);
            log.info("Seeded Admin user: admin@inventory.com / admin123");
        }
        if (!userRepository.existsByEmail("soundhar@gmail.com")) {
            User staff = new User("Soundhar", "soundhar@gmail.com", passwordEncoder.encode("123"), Role.STAFF);
            userRepository.save(staff);
            log.info("Seeded Staff user: soundhar@gmail.com / 123");
        }
        if (!userRepository.existsByEmail("staff@inventory.com")) {
            User staff = new User("Staff Member", "staff@inventory.com", passwordEncoder.encode("staff123"), Role.STAFF);
            userRepository.save(staff);
            log.info("Seeded Staff user: staff@inventory.com / staff123");
        }
        if (!userRepository.existsByEmail("viewer@gmail.com")) {
            User viewer = new User("Viewer Account", "viewer@gmail.com", passwordEncoder.encode("123"), Role.VIEWER);
            userRepository.save(viewer);
            log.info("Seeded Viewer user: viewer@gmail.com / 123");
        }
        if (!userRepository.existsByEmail("viewer@inventory.com")) {
            User viewer = new User("Viewer User", "viewer@inventory.com", passwordEncoder.encode("viewer123"), Role.VIEWER);
            userRepository.save(viewer);
            log.info("Seeded Viewer user: viewer@inventory.com / viewer123");
        }
    }

    private void cleanupDummyTestProducts() {
        List<Product> dummies = productRepository.findAll().stream()
                .filter(p -> p.getName() != null && (
                        p.getName().startsWith("Test Stock Item") ||
                        p.getName().startsWith("Limited Stock Item") ||
                        p.getName().startsWith("Clean Deletable Item") ||
                        p.getName().startsWith("Temporary Inventory Test Item") ||
                        p.getName().startsWith("Integration Test Item") ||
                        p.getName().matches(".*\\d{10,}.*")
                ))
                .toList();

        for (Product dummy : dummies) {
            try {
                stockMovementRepository.deleteAll(stockMovementRepository.findByProductIdOrderByTimestampDesc(dummy.getId()));
                alertRepository.deleteByReferenceTypeAndReferenceId("PRODUCT", String.valueOf(dummy.getId()));
                productRepository.delete(dummy);
                log.info("Cleaned up dummy test product: {}", dummy.getName());
            } catch (Exception e) {
                log.warn("Could not delete dummy product {}: {}", dummy.getName(), e.getMessage());
            }
        }
    }

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name, description)));
    }

    private Supplier getOrCreateSupplier(String name, String contact, String email, String phone, String address) {
        return supplierRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> supplierRepository.save(new Supplier(name, contact, email, phone, address)));
    }

    private void seedCategoriesAndSuppliers() {
        // Categories
        getOrCreateCategory("Electronics", "Electronic gadgets, smartphones, 4K televisions, and audio gear");
        getOrCreateCategory("Computers", "Laptops, desktop systems, high-performance monitors, and processors");
        getOrCreateCategory("Home Appliances", "Smart kitchen appliances, refrigerators, washing machines, and home equipment");
        getOrCreateCategory("Computer Accessories", "Computer peripherals, wireless mice, keyboards, hubs, storage, and networking");

        // Suppliers
        getOrCreateSupplier("Samsung India", "Rajesh Sharma", "sales@samsung-india.com", "+91-80-41234567", "Samsung India Electronics Pvt Ltd, Gurugram, Haryana");
        getOrCreateSupplier("Apple India", "Priya Menon", "b2b@apple-india.com", "+91-80-67890123", "Apple India Pvt Ltd, UB City, Bengaluru, Karnataka");
        getOrCreateSupplier("OnePlus India", "Vikas Agarwal", "orders@oneplus-india.com", "+91-80-33445566", "OnePlus Technology India, UB City, Bengaluru, Karnataka");
        getOrCreateSupplier("Sony India", "Amit Verma", "corporate@sony-india.com", "+91-11-66005500", "Sony India Pvt Ltd, Mohan Cooperative, New Delhi");
        getOrCreateSupplier("LG Electronics India", "Suresh Kumar", "supply@lg-india.com", "+91-120-2560940", "LG Electronics India, Greater Noida, Uttar Pradesh");
        getOrCreateSupplier("Dell India", "Neha Patel", "enterprisestore@dell-india.com", "+91-80-25003000", "Dell International Services, Divyasree Greens, Bengaluru");
        getOrCreateSupplier("HP India", "Rohan Mehta", "commercial@hp-india.com", "+91-80-40302000", "HP India Sales Pvt Ltd, Golf Course Road, Gurugram");
        getOrCreateSupplier("Lenovo India", "Ananya Roy", "b2b@lenovo-india.com", "+91-80-30533000", "Lenovo India Pvt Ltd, Outer Ring Road, Bengaluru");
        getOrCreateSupplier("Philips India", "Sanjay Gupta", "consumer@philips-india.com", "+91-124-4606000", "Philips India Ltd, DLF Cyber City, Gurugram");
        getOrCreateSupplier("Bajaj Electricals", "Manoj Joshi", "sales@bajajelectricals.com", "+91-22-22043841", "Bajaj Electricals Ltd, Sion West, Mumbai");
        getOrCreateSupplier("Logitech India", "Deepa Nair", "distribution@logitech-india.com", "+91-22-67894500", "Logitech Electronic India, Bandra Kurla Complex, Mumbai");
        getOrCreateSupplier("SanDisk India", "Arjun Nair", "orders@sandisk-india.com", "+91-80-49001000", "Western Digital SanDisk India, Whitefield, Bengaluru");
        getOrCreateSupplier("TP-Link Technologies", "Tarun Bansal", "sales@tp-link-india.com", "+91-22-45678900", "TP-Link India Pvt Ltd, Andheri East, Mumbai");
    }

    private void seedProducts() {
        if (productRepository.findBySku("SKU-ELEC-S25").isPresent()) {
            log.info("Realistic products already seeded.");
            return;
        }

        Category electronics = getOrCreateCategory("Electronics", "Electronic gadgets, smartphones, 4K televisions, and audio gear");
        Category computers = getOrCreateCategory("Computers", "Laptops, desktop systems, high-performance monitors, and processors");
        Category appliances = getOrCreateCategory("Home Appliances", "Smart kitchen appliances, refrigerators, washing machines, and home equipment");
        Category accessories = getOrCreateCategory("Computer Accessories", "Computer peripherals, wireless mice, keyboards, hubs, storage, and networking");

        Supplier samsung = supplierRepository.findByNameIgnoreCase("Samsung India").orElse(null);
        Supplier apple = supplierRepository.findByNameIgnoreCase("Apple India").orElse(null);
        Supplier oneplus = supplierRepository.findByNameIgnoreCase("OnePlus India").orElse(null);
        Supplier sony = supplierRepository.findByNameIgnoreCase("Sony India").orElse(null);
        Supplier lg = supplierRepository.findByNameIgnoreCase("LG Electronics India").orElse(null);
        Supplier dell = supplierRepository.findByNameIgnoreCase("Dell India").orElse(null);
        Supplier hp = supplierRepository.findByNameIgnoreCase("HP India").orElse(null);
        Supplier lenovo = supplierRepository.findByNameIgnoreCase("Lenovo India").orElse(null);
        Supplier philips = supplierRepository.findByNameIgnoreCase("Philips India").orElse(null);
        Supplier bajaj = supplierRepository.findByNameIgnoreCase("Bajaj Electricals").orElse(null);
        Supplier logitech = supplierRepository.findByNameIgnoreCase("Logitech India").orElse(null);
        Supplier sandisk = supplierRepository.findByNameIgnoreCase("SanDisk India").orElse(null);
        Supplier tplink = supplierRepository.findByNameIgnoreCase("TP-Link Technologies").orElse(null);

        // 1. Samsung Galaxy S25
        Product p1 = saveProductIfAbsent("Samsung Galaxy S25", "SKU-ELEC-S25", electronics, samsung, 25, 5, new BigDecimal("65000.00"), new BigDecimal("74999.00"), "Flagship Samsung 5G smartphone with AI features");
        // 2. iPhone 16
        saveProductIfAbsent("iPhone 16", "SKU-ELEC-IP16", electronics, apple, 18, 5, new BigDecimal("70000.00"), new BigDecimal("79900.00"), "Latest Apple smartphone with A18 chip and advanced camera system");
        // 3. Sony Bravia 55-inch 4K TV
        saveProductIfAbsent("Sony Bravia 55-inch 4K TV", "SKU-ELEC-SB55", electronics, sony, 8, 3, new BigDecimal("52000.00"), new BigDecimal("64999.00"), "55-inch 4K Ultra HD Smart Google TV with Dolby Vision and Atmos");
        // 4. Dell Inspiron 15 Laptop
        saveProductIfAbsent("Dell Inspiron 15 Laptop", "SKU-COMP-DELL15", computers, dell, 4, 5, new BigDecimal("48000.00"), new BigDecimal("56999.00"), "15.6-inch FHD laptop with Intel Core processor and 16GB RAM");
        // 5. LG 260L Refrigerator
        saveProductIfAbsent("LG 260L Refrigerator", "SKU-APP-LG260", appliances, lg, 12, 3, new BigDecimal("22000.00"), new BigDecimal("27490.00"), "Double door frost-free smart inverter compressor refrigerator");
        // 6. OnePlus 13
        saveProductIfAbsent("OnePlus 13", "SKU-ELEC-OP13", electronics, oneplus, 15, 5, new BigDecimal("58000.00"), new BigDecimal("69999.00"), "Flagship smartphone with Snapdragon 8 Elite and Hasselblad camera");
        // 7. LG 43-inch Smart TV
        saveProductIfAbsent("LG 43-inch Smart TV", "SKU-ELEC-LG43", electronics, lg, 2, 3, new BigDecimal("26000.00"), new BigDecimal("32990.00"), "43-inch 4K UHD LED Smart webOS TV with HDR10 Pro");
        // 8. HP Pavilion Laptop
        saveProductIfAbsent("HP Pavilion Laptop", "SKU-COMP-HPPAV", computers, hp, 12, 4, new BigDecimal("54000.00"), new BigDecimal("62500.00"), "Thin and light performance laptop with backlit keyboard");
        // 9. Lenovo IdeaPad Slim 5
        saveProductIfAbsent("Lenovo IdeaPad Slim 5", "SKU-COMP-LENSLIM", computers, lenovo, 0, 5, new BigDecimal("51000.00"), new BigDecimal("59990.00"), "Ultra-slim lightweight laptop with AMD Ryzen processor");
        // 10. Apple AirPods Pro
        saveProductIfAbsent("Apple AirPods Pro", "SKU-ELEC-AIRPOD", electronics, apple, 30, 8, new BigDecimal("19000.00"), new BigDecimal("24900.00"), "Wireless noise-cancelling earbuds with MagSafe charging case");
        // 11. Samsung Galaxy Buds
        saveProductIfAbsent("Samsung Galaxy Buds", "SKU-ELEC-GABUDS", electronics, samsung, 22, 6, new BigDecimal("9500.00"), new BigDecimal("12999.00"), "True wireless earbuds with active noise cancellation");
        // 12. Samsung Washing Machine
        saveProductIfAbsent("Samsung Washing Machine", "SKU-APP-SAMWASH", appliances, samsung, 9, 3, new BigDecimal("18500.00"), new BigDecimal("23990.00"), "Fully automatic front load washing machine with Hygiene Steam");
        // 13. Philips Air Fryer
        saveProductIfAbsent("Philips Air Fryer", "SKU-APP-PHILAF", appliances, philips, 16, 5, new BigDecimal("6800.00"), new BigDecimal("8999.00"), "Rapid Air technology 4.1L digital air fryer for healthy cooking");
        // 14. Bajaj Mixer Grinder
        saveProductIfAbsent("Bajaj Mixer Grinder", "SKU-APP-BAJMIX", appliances, bajaj, 3, 5, new BigDecimal("2400.00"), new BigDecimal("3299.00"), "750W durable mixer grinder with 3 stainless steel jars");
        // 15. Philips LED TV
        saveProductIfAbsent("Philips LED TV", "SKU-APP-PHILTV", appliances, philips, 6, 2, new BigDecimal("14000.00"), new BigDecimal("18499.00"), "32-inch HD Ready LED Android Smart TV with stereo speakers");
        // 16. Logitech Wireless Mouse
        saveProductIfAbsent("Logitech Wireless Mouse", "SKU-ACC-LOGIM", accessories, logitech, 50, 10, new BigDecimal("850.00"), new BigDecimal("1295.00"), "Compact 2.4GHz wireless optical mouse with 12-month battery life");
        // 17. HP Wireless Keyboard
        saveProductIfAbsent("HP Wireless Keyboard", "SKU-ACC-HPKB", accessories, hp, 35, 8, new BigDecimal("1100.00"), new BigDecimal("1699.00"), "Slim multi-device wireless keyboard with quiet keys");
        // 18. Dell USB-C Hub
        saveProductIfAbsent("Dell USB-C Hub", "SKU-ACC-DELLHUB", accessories, dell, 20, 5, new BigDecimal("2800.00"), new BigDecimal("3999.00"), "7-in-1 multi-port adapter USB-C hub with 4K HDMI");
        // 19. SanDisk 128GB Pen Drive
        saveProductIfAbsent("SanDisk 128GB Pen Drive", "SKU-ACC-SAND128", accessories, sandisk, 60, 15, new BigDecimal("650.00"), new BigDecimal("999.00"), "Ultra Dual Drive Go 128GB USB Type-C and Type-A flash drive");
        // 20. TP-Link WiFi Router
        saveProductIfAbsent("TP-Link WiFi Router", "SKU-ACC-TPLINK", accessories, tplink, 14, 4, new BigDecimal("1450.00"), new BigDecimal("2199.00"), "AC1200 dual band wireless gigabit router with high-gain antennas");

        // Record stock movement for Product 1 so reference protection tests pass
        if (p1 != null && stockMovementRepository.countByProductId(p1.getId()) == 0) {
            stockMovementRepository.save(new StockMovement(p1, 25, MovementType.PURCHASE, 0, 25, "PO-INIT-001", "admin@inventory.com", "Initial catalog batch receipt"));
        }

        log.info("Seeded 20 realistic products.");
    }

    private Product saveProductIfAbsent(String name, String sku, Category category, Supplier supplier,
                                        int quantity, int minStock, BigDecimal purchasePrice, BigDecimal sellingPrice,
                                        String description) {
        return productRepository.findBySku(sku).orElseGet(() -> {
            Product p = new Product();
            p.setName(name);
            p.setSku(sku);
            p.setCategory(category);
            p.setSupplier(supplier);
            p.setQuantity(quantity);
            p.setMinimumStockLevel(minStock);
            p.setPurchasePrice(purchasePrice);
            p.setSellingPrice(sellingPrice);
            p.setDescription(description);
            p.updateStockStatus();
            return productRepository.save(p);
        });
    }

    private void seedOrdersAndAlerts() {
        seedAlerts();
        seedSalesOrders();
        seedPurchaseOrders();
    }

    private void seedAlerts() {
        if (alertRepository.count() == 0) {
            Product lenovo = productRepository.findBySku("SKU-COMP-LENSLIM").orElse(null);
            Product dell = productRepository.findBySku("SKU-COMP-DELL15").orElse(null);
            Product lgTv = productRepository.findBySku("SKU-ELEC-LG43").orElse(null);

            if (lenovo != null) {
                alertRepository.save(new Alert(AlertType.CRITICAL, "Out of Stock", "Lenovo IdeaPad Slim 5 is completely out of stock!", "PRODUCT", String.valueOf(lenovo.getId())));
            }
            if (dell != null) {
                alertRepository.save(new Alert(AlertType.WARNING, "Low Stock Warning", "Dell Inspiron 15 Laptop stock is running low (4 remaining, minimum threshold: 5)", "PRODUCT", String.valueOf(dell.getId())));
            }
            if (lgTv != null) {
                alertRepository.save(new Alert(AlertType.WARNING, "Low Stock Warning", "LG 43-inch Smart TV stock is running low (2 remaining, minimum threshold: 3)", "PRODUCT", String.valueOf(lgTv.getId())));
            }
            alertRepository.save(new Alert(AlertType.INFO, "System Notice", "Weekly automated inventory backup completed successfully", "SYSTEM", "0"));

            log.info("Seeded initial Alerts.");
        }
    }

    private void seedSalesOrders() {
        if (salesOrderRepository.count() == 0) {
            Product dellHub = productRepository.findBySku("SKU-ACC-DELLHUB").orElse(null);
            Product logiMouse = productRepository.findBySku("SKU-ACC-LOGIM").orElse(null);
            Product sandisk = productRepository.findBySku("SKU-ACC-SAND128").orElse(null);
            Product airpods = productRepository.findBySku("SKU-ELEC-AIRPOD").orElse(null);
            Product buds = productRepository.findBySku("SKU-ELEC-GABUDS").orElse(null);
            Product router = productRepository.findBySku("SKU-ACC-TPLINK").orElse(null);

            if (dellHub != null) {
                SalesOrder so1 = new SalesOrder("ORD-1001", "Alice Johnson", "alice@example.com", SalesStatus.DELIVERED, new BigDecimal("3999.00"), "Delivered successfully", "staff@inventory.com");
                so1.addItem(new SalesOrderItem(dellHub, 1, new BigDecimal("3999.00")));
                salesOrderRepository.save(so1);
            }
            if (logiMouse != null) {
                SalesOrder so2 = new SalesOrder("ORD-1002", "Bob Smith", "bob@example.com", SalesStatus.DELIVERED, new BigDecimal("2590.00"), "Delivered to office", "staff@inventory.com");
                so2.addItem(new SalesOrderItem(logiMouse, 2, new BigDecimal("1295.00")));
                salesOrderRepository.save(so2);
            }
            if (sandisk != null) {
                SalesOrder so3 = new SalesOrder("ORD-1003", "Charlie Brown", "charlie@example.com", SalesStatus.SHIPPED, new BigDecimal("999.00"), "Shipped via courier", "staff@inventory.com");
                so3.addItem(new SalesOrderItem(sandisk, 1, new BigDecimal("999.00")));
                salesOrderRepository.save(so3);
            }
            if (airpods != null) {
                SalesOrder so4 = new SalesOrder("ORD-1004", "Pooja Sharma", "pooja@example.com", SalesStatus.PROCESSING, new BigDecimal("24900.00"), "Customer requested gift wrap", "staff@inventory.com");
                so4.addItem(new SalesOrderItem(airpods, 1, new BigDecimal("24900.00")));
                salesOrderRepository.save(so4);
            }
            if (buds != null && router != null) {
                SalesOrder so5 = new SalesOrder("ORD-1005", "Vikram Malhotra", "vikram@example.com", SalesStatus.DELIVERED, new BigDecimal("15198.00"), "Corporate desk setup order", "admin@inventory.com");
                so5.addItem(new SalesOrderItem(buds, 1, new BigDecimal("12999.00")));
                so5.addItem(new SalesOrderItem(router, 1, new BigDecimal("2199.00")));
                salesOrderRepository.save(so5);
            }
            log.info("Seeded initial Sales Orders with items.");
        } else {
            // Ensure any existing order has items linked so totalItemsSold and COGS are accurate
            List<SalesOrder> existing = salesOrderRepository.findAll();
            for (SalesOrder order : existing) {
                if (order.getItems() == null || order.getItems().isEmpty()) {
                    if ("ORD-001".equals(order.getOrderNumber())) {
                        productRepository.findBySku("SKU-ACC-DELLHUB").ifPresent(p -> order.addItem(new SalesOrderItem(p, 1, p.getSellingPrice())));
                    } else if ("ORD-002".equals(order.getOrderNumber())) {
                        productRepository.findBySku("SKU-ACC-LOGIM").ifPresent(p -> order.addItem(new SalesOrderItem(p, 2, p.getSellingPrice())));
                    } else if ("ORD-003".equals(order.getOrderNumber())) {
                        productRepository.findBySku("SKU-ACC-SAND128").ifPresent(p -> order.addItem(new SalesOrderItem(p, 1, p.getSellingPrice())));
                    } else if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
                        productRepository.findAll().stream()
                                .filter(p -> p.getSellingPrice().compareTo(order.getTotalAmount()) == 0)
                                .findFirst()
                                .ifPresent(p -> order.addItem(new SalesOrderItem(p, 1, p.getSellingPrice())));
                    }
                    salesOrderRepository.save(order);
                }
            }
        }
    }

    private void seedPurchaseOrders() {
        if (purchaseOrderRepository.count() == 0) {
            Supplier samsung = supplierRepository.findByNameIgnoreCase("Samsung India").orElse(null);
            Supplier apple = supplierRepository.findByNameIgnoreCase("Apple India").orElse(null);
            Supplier logitech = supplierRepository.findByNameIgnoreCase("Logitech India").orElse(null);

            Product buds = productRepository.findBySku("SKU-ELEC-GABUDS").orElse(null);
            Product airpods = productRepository.findBySku("SKU-ELEC-AIRPOD").orElse(null);
            Product logiMouse = productRepository.findBySku("SKU-ACC-LOGIM").orElse(null);

            if (samsung != null && buds != null) {
                PurchaseOrder po1 = new PurchaseOrder("PO-2026-001", samsung, PurchaseStatus.RECEIVED, new BigDecimal("95000.00"), "Stock replenishment batch", "admin@inventory.com");
                po1.addItem(new PurchaseOrderItem(buds, 10, new BigDecimal("9500.00")));
                purchaseOrderRepository.save(po1);
            }
            if (apple != null && airpods != null) {
                PurchaseOrder po2 = new PurchaseOrder("PO-2026-002", apple, PurchaseStatus.RECEIVED, new BigDecimal("95000.00"), "Quarterly audio inventory", "admin@inventory.com");
                po2.addItem(new PurchaseOrderItem(airpods, 5, new BigDecimal("19000.00")));
                purchaseOrderRepository.save(po2);
            }
            if (logitech != null && logiMouse != null) {
                PurchaseOrder po3 = new PurchaseOrder("PO-2026-003", logitech, PurchaseStatus.RECEIVED, new BigDecimal("17000.00"), "Accessories stock order", "admin@inventory.com");
                po3.addItem(new PurchaseOrderItem(logiMouse, 20, new BigDecimal("850.00")));
                purchaseOrderRepository.save(po3);
            }
            log.info("Seeded initial Purchase Orders with items.");
        }
    }
}

