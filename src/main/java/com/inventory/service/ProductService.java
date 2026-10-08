package com.inventory.service;

import com.inventory.dto.ProductRequest;
import com.inventory.dto.ProductResponse;
import com.inventory.dto.StockEntryRequest;
import com.inventory.entity.*;
import com.inventory.exception.ConflictException;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.InvalidOperationException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.AlertRepository;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.PurchaseOrderRepository;
import com.inventory.repository.ReturnOrderRepository;
import com.inventory.repository.SalesOrderRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.SupplierRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ReturnOrderRepository returnOrderRepository;
    private final AlertRepository alertRepository;
    private final StockMovementService stockMovementService;
    private final AlertService alertService;
    private final ActivityLogService activityLogService;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          SupplierRepository supplierRepository,
                          StockMovementRepository stockMovementRepository,
                          SalesOrderRepository salesOrderRepository,
                          PurchaseOrderRepository purchaseOrderRepository,
                          ReturnOrderRepository returnOrderRepository,
                          AlertRepository alertRepository,
                          StockMovementService stockMovementService,
                          AlertService alertService,
                          ActivityLogService activityLogService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.returnOrderRepository = returnOrderRepository;
        this.alertRepository = alertRepository;
        this.stockMovementService = stockMovementService;
        this.alertService = alertService;
        this.activityLogService = activityLogService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "system";
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllByOrderByIdAsc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    public List<ProductResponse> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(query.trim()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String name = request.getName().trim();

        String sku = request.getSku();
        if (sku != null && !sku.trim().isEmpty()) {
            sku = sku.trim().toUpperCase();
            if (productRepository.existsBySku(sku)) {
                throw new DuplicateResourceException("Product with SKU '" + sku + "' already exists");
            }
        } else {
            sku = "SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Category category = resolveCategory(request);
        Supplier supplier = resolveSupplier(request);

        Integer initialStock = request.getStock() != null ? request.getStock() : (request.getQuantity() != null ? request.getQuantity() : 0);
        if (initialStock < 0) {
            throw new InvalidOperationException("Stock quantity cannot be negative");
        }

        Integer minStock = request.getMinimumStockLevel() != null ? request.getMinimumStockLevel() : 10;
        BigDecimal sellingPrice = request.getPrice() != null ? request.getPrice() : (request.getSellingPrice() != null ? request.getSellingPrice() : BigDecimal.ZERO);
        BigDecimal purchasePrice = request.getPurchasePrice() != null ? request.getPurchasePrice() : BigDecimal.ZERO;

        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setCategory(category);
        product.setSupplier(supplier);
        product.setDescription(request.getDescription());
        product.setQuantity(initialStock);
        product.setMinimumStockLevel(minStock);
        product.setPurchasePrice(purchasePrice);
        product.setSellingPrice(sellingPrice);
        product.setExpiryDate(request.getExpiryDate());
        product.updateStockStatus();

        Product saved = productRepository.save(product);

        // Record initial stock movement if > 0
        if (initialStock > 0) {
            stockMovementService.recordMovement(
                    saved,
                    initialStock,
                    MovementType.ADJUSTMENT,
                    0,
                    initialStock,
                    "INITIAL_SETUP",
                    getCurrentUserEmail(),
                    "Initial stock entry on product creation"
            );
        }

        // Check alerts
        alertService.checkProductAlerts(saved);

        activityLogService.log("PRODUCT_CREATE", "INVENTORY", "Created product: " + saved.getName() + " (SKU: " + saved.getSku() + ")");

        return mapToResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            product.setName(request.getName().trim());
        }

        if (request.getSku() != null && !request.getSku().trim().isEmpty()) {
            String newSku = request.getSku().trim().toUpperCase();
            if (!newSku.equalsIgnoreCase(product.getSku()) && productRepository.existsBySku(newSku)) {
                throw new DuplicateResourceException("Product with SKU '" + newSku + "' already exists");
            }
            product.setSku(newSku);
        }

        Category category = resolveCategory(request);
        if (category != null) {
            product.setCategory(category);
        }

        Supplier supplier = resolveSupplier(request);
        if (supplier != null) {
            product.setSupplier(supplier);
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getMinimumStockLevel() != null) {
            product.setMinimumStockLevel(request.getMinimumStockLevel());
        }

        if (request.getPrice() != null) {
            product.setSellingPrice(request.getPrice());
        } else if (request.getSellingPrice() != null) {
            product.setSellingPrice(request.getSellingPrice());
        }

        if (request.getPurchasePrice() != null) {
            product.setPurchasePrice(request.getPurchasePrice());
        }

        if (request.getExpiryDate() != null) {
            product.setExpiryDate(request.getExpiryDate());
        }

        // Handle quantity update if provided
        Integer previous = product.getQuantity();
        Integer newStock = request.getStock() != null ? request.getStock() : request.getQuantity();
        boolean stockChanged = false;
        if (newStock != null && !newStock.equals(product.getQuantity())) {
            if (newStock < 0) {
                throw new InvalidOperationException("Stock quantity cannot be negative");
            }
            int diff = newStock - previous;
            product.setQuantity(newStock);
            stockChanged = true;

            stockMovementService.recordMovement(
                    product,
                    Math.abs(diff),
                    MovementType.ADJUSTMENT,
                    previous,
                    newStock,
                    "MANUAL_ADJUSTMENT",
                    getCurrentUserEmail(),
                    "Manual stock adjustment by user"
            );
        }

        product.updateStockStatus();
        Product saved = productRepository.save(product);

        alertService.checkProductAlerts(saved);

        if (stockChanged) {
            activityLogService.log("STOCK_ADJUSTMENT", "STOCK", "Adjusted stock for product: " + saved.getName() + " from " + previous + " to " + newStock);
        } else {
            activityLogService.log("PRODUCT_UPDATE", "INVENTORY", "Updated product: " + saved.getName() + " (SKU: " + saved.getSku() + ")");
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        long stockMovements = stockMovementRepository.countByProductId(id);
        long salesItems = salesOrderRepository.countSalesOrderItemsByProductId(id);
        long purchaseItems = purchaseOrderRepository.countPurchaseOrderItemsByProductId(id);
        long returnItems = returnOrderRepository.countReturnOrderItemsByProductId(id);

        long totalReferences = stockMovements + salesItems + purchaseItems + returnItems;
        if (totalReferences > 0) {
            StringBuilder sb = new StringBuilder();
            if (stockMovements > 0) sb.append(stockMovements).append(" stock movement(s), ");
            if (salesItems > 0) sb.append(salesItems).append(" sales order item(s), ");
            if (purchaseItems > 0) sb.append(purchaseItems).append(" purchase order item(s), ");
            if (returnItems > 0) sb.append(returnItems).append(" return order item(s), ");
            String details = sb.substring(0, sb.length() - 2);

            throw new ConflictException(
                    "Cannot delete product '" + product.getName() + "' (SKU: " + product.getSku() +
                    ") because it has existing transaction history: " + details +
                    ". To preserve inventory ledger integrity and audit history, this product cannot be deleted."
            );
        }

        try {
            alertRepository.deleteByReferenceTypeAndReferenceId("PRODUCT", String.valueOf(id));
            productRepository.delete(product);
            activityLogService.log("PRODUCT_DELETE", "INVENTORY", "Deleted product: " + product.getName() + " (SKU: " + product.getSku() + ")");
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException(
                    "Cannot delete product '" + product.getName() +
                    "' due to database foreign-key constraints."
            );
        }
    }

    @Transactional
    public ProductResponse receiveStock(StockEntryRequest request) {
        Product product = null;
        if (request.getProductId() != null) {
            product = productRepository.findByIdWithLock(request.getProductId())
                    .orElse(null);
        }

        if (product == null && request.getItemName() != null && !request.getItemName().trim().isEmpty()) {
            String name = request.getItemName().trim();
            List<Product> matches = productRepository.searchProducts(name);
            if (!matches.isEmpty()) {
                product = productRepository.findByIdWithLock(matches.get(0).getId()).orElse(null);
            } else {
                // Create new product on-the-fly for stock entry
                product = new Product();
                product.setName(name);
                product.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                product.setQuantity(0);
                product.setMinimumStockLevel(10);
                product.setPurchasePrice(BigDecimal.ZERO);
                product.setSellingPrice(BigDecimal.ZERO);

                if (request.getSupplier() != null && !request.getSupplier().trim().isEmpty()) {
                    Supplier s = supplierRepository.findByNameIgnoreCase(request.getSupplier().trim())
                            .orElseGet(() -> {
                                Supplier newS = new Supplier();
                                newS.setName(request.getSupplier().trim());
                                return supplierRepository.save(newS);
                            });
                    product.setSupplier(s);
                }

                product = productRepository.save(product);
            }
        }

        if (product == null) {
            throw new ResourceNotFoundException("Product could not be identified or created");
        }

        int qtyReceived = request.getQuantity();
        if (qtyReceived <= 0) {
            throw new InvalidOperationException("Received quantity must be positive");
        }

        int previousStock = product.getQuantity();
        int resultingStock = previousStock + qtyReceived;

        product.setQuantity(resultingStock);
        product.updateStockStatus();
        Product saved = productRepository.save(product);

        String remarks = request.getRemarks() != null ? request.getRemarks() : "Stock received from " + (request.getSupplier() != null ? request.getSupplier() : "Supplier");

        stockMovementService.recordMovement(
                saved,
                qtyReceived,
                MovementType.PURCHASE,
                previousStock,
                resultingStock,
                "RECEIPT-" + System.currentTimeMillis(),
                getCurrentUserEmail(),
                remarks
        );

        alertService.checkProductAlerts(saved);

        return mapToResponse(saved);
    }

    @Transactional
    public Product reduceStock(Long productId, int quantityToReduce, String referenceOrder, String remarks) {
        if (quantityToReduce <= 0) {
            throw new InvalidOperationException("Quantity to reduce must be positive");
        }

        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int currentStock = product.getQuantity();
        if (currentStock < quantityToReduce) {
            throw new InsufficientStockException("Insufficient stock for product '" + product.getName() +
                    "'. Available: " + currentStock + ", Requested: " + quantityToReduce);
        }

        int resultingStock = currentStock - quantityToReduce;
        product.setQuantity(resultingStock);
        product.updateStockStatus();
        Product saved = productRepository.save(product);

        stockMovementService.recordMovement(
                saved,
                quantityToReduce,
                MovementType.SALE,
                currentStock,
                resultingStock,
                referenceOrder,
                getCurrentUserEmail(),
                remarks
        );

        alertService.checkProductAlerts(saved);

        return saved;
    }

    @Transactional
    public Product increaseStock(Long productId, int quantityToAdd, MovementType movementType, String referenceOrder, String remarks) {
        if (quantityToAdd <= 0) {
            throw new InvalidOperationException("Quantity to add must be positive");
        }

        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int currentStock = product.getQuantity();
        int resultingStock = currentStock + quantityToAdd;

        product.setQuantity(resultingStock);
        product.updateStockStatus();
        Product saved = productRepository.save(product);

        stockMovementService.recordMovement(
                saved,
                quantityToAdd,
                movementType,
                currentStock,
                resultingStock,
                referenceOrder,
                getCurrentUserEmail(),
                remarks
        );

        alertService.checkProductAlerts(saved);

        return saved;
    }

    private Category resolveCategory(ProductRequest request) {
        if (request.getCategoryId() != null) {
            return categoryRepository.findById(request.getCategoryId()).orElse(null);
        }
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            String name = request.getCategory().trim();
            return categoryRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> categoryRepository.save(new Category(name, "Auto-created category")));
        }
        return null;
    }

    private Supplier resolveSupplier(ProductRequest request) {
        if (request.getSupplierId() != null) {
            return supplierRepository.findById(request.getSupplierId()).orElse(null);
        }
        if (request.getSupplier() != null && !request.getSupplier().trim().isEmpty()) {
            String name = request.getSupplier().trim();
            return supplierRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> supplierRepository.save(new Supplier(name, null, null, null, null)));
        }
        return null;
    }

    public ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        if (product.getCategory() != null) {
            response.setCategory(product.getCategory().getName());
            response.setCategoryId(product.getCategory().getId());
        }
        if (product.getSupplier() != null) {
            response.setSupplier(product.getSupplier().getName());
            response.setSupplierId(product.getSupplier().getId());
        }
        response.setDescription(product.getDescription());
        response.setQuantity(product.getQuantity());
        response.setStock(product.getQuantity());
        response.setMinimumStockLevel(product.getMinimumStockLevel());
        response.setPurchasePrice(product.getPurchasePrice());
        response.setSellingPrice(product.getSellingPrice());
        response.setPrice(product.getSellingPrice());

        // Status mapping to match frontend display strings ("IN STOCK", "LOW STOCK", "OUT OF STOCK")
        if (product.getStatus() == StockStatus.AVAILABLE) {
            response.setStatus("IN STOCK");
        } else if (product.getStatus() == StockStatus.LOW_STOCK) {
            response.setStatus("LOW STOCK");
        } else {
            response.setStatus("OUT OF STOCK");
        }

        response.setExpiryDate(product.getExpiryDate());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
