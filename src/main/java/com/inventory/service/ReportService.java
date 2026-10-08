package com.inventory.service;

import com.inventory.dto.DeadStockItemResponse;
import com.inventory.dto.DeadStockReportResponse;
import com.inventory.dto.FinancialReportResponse;
import com.inventory.dto.InventoryReportResponse;
import com.inventory.entity.*;
import com.inventory.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ProductRepository productRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CategoryRepository categoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ReturnOrderRepository returnOrderRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogService activityLogService;

    public ReportService(ProductRepository productRepository,
                         SalesOrderRepository salesOrderRepository,
                         CategoryRepository categoryRepository,
                         PurchaseOrderRepository purchaseOrderRepository,
                         SupplierRepository supplierRepository,
                         ReturnOrderRepository returnOrderRepository,
                         UserRepository userRepository,
                         StockMovementRepository stockMovementRepository,
                         ActivityLogRepository activityLogRepository,
                         ActivityLogService activityLogService) {
        this.productRepository = productRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.categoryRepository = categoryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.returnOrderRepository = returnOrderRepository;
        this.userRepository = userRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.activityLogRepository = activityLogRepository;
        this.activityLogService = activityLogService;
    }

    public FinancialReportResponse getFinancialReport() {
        BigDecimal totalRevenue = salesOrderRepository.calculateTotalSalesValue();
        BigDecimal totalCost = salesOrderRepository.calculateTotalCostOfGoodsSold();

        if (totalCost == null || totalCost.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal purchasesVal = purchaseOrderRepository.calculateTotalPurchasesValue();
            if (purchasesVal != null && purchasesVal.compareTo(BigDecimal.ZERO) > 0) {
                totalCost = purchasesVal;
            }
        }

        long totalOrders = salesOrderRepository.count();
        Long totalItemsSold = salesOrderRepository.calculateTotalItemsSold();

        return new FinancialReportResponse(
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                totalCost != null ? totalCost : BigDecimal.ZERO,
                totalOrders,
                totalItemsSold != null ? totalItemsSold : 0
        );
    }

    public InventoryReportResponse getInventoryReport() {
        long totalProducts = productRepository.count();
        Long totalStock = productRepository.calculateTotalStock();
        long lowStockCount = productRepository.countByStatus(StockStatus.LOW_STOCK);
        long outOfStockCount = productRepository.countByStatus(StockStatus.OUT_OF_STOCK);

        Map<String, Long> categoryDistribution = new HashMap<>();
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            long count = productRepository.findByCategoryId(category.getId()).size();
            categoryDistribution.put(category.getName(), count);
        }

        return new InventoryReportResponse(
                totalProducts,
                totalStock != null ? totalStock : 0,
                lowStockCount,
                outOfStockCount,
                categoryDistribution
        );
    }

    public String exportReportsCsv() {
        return exportProductsCsv();
    }

    public String exportProductsCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Products / Inventory CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("Product ID,Product Name,Category,Supplier,SKU,Quantity,Minimum Stock Level,Purchase Price,Selling Price,Status,Created Date\n");

        List<Product> products = productRepository.findAll();
        for (Product p : products) {
            csv.append(p.getId()).append(",")
                    .append(escape(p.getName())).append(",")
                    .append(escape(p.getCategory() != null ? p.getCategory().getName() : "")).append(",")
                    .append(escape(p.getSupplier() != null ? p.getSupplier().getName() : "")).append(",")
                    .append(escape(p.getSku())).append(",")
                    .append(p.getQuantity()).append(",")
                    .append(p.getMinimumStockLevel()).append(",")
                    .append(p.getPurchasePrice() != null ? p.getPurchasePrice() : "0.00").append(",")
                    .append(p.getSellingPrice() != null ? p.getSellingPrice() : "0.00").append(",")
                    .append(p.getStatus() != null ? p.getStatus().name() : "").append(",")
                    .append(escape(p.getCreatedAt() != null ? p.getCreatedAt().toString() : "")).append("\n");
        }
        return csv.toString();
    }

    public String exportSuppliersCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Suppliers CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("Supplier ID,Supplier Name,Company Name,Contact Person,Email,Phone,GST Number,Address,Status\n");

        List<Supplier> suppliers = supplierRepository.findAll();
        for (Supplier s : suppliers) {
            csv.append(s.getId()).append(",")
                    .append(escape(s.getName())).append(",")
                    .append(escape(s.getCompanyName())).append(",")
                    .append(escape(s.getContactPerson())).append(",")
                    .append(escape(s.getEmail())).append(",")
                    .append(escape(s.getPhone())).append(",")
                    .append(escape(s.getGstNumber())).append(",")
                    .append(escape(s.getAddress())).append(",")
                    .append(escape(s.getStatus())).append("\n");
        }
        return csv.toString();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String exportPurchaseOrdersCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Purchase Orders CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("PO ID,PO Number,Supplier,Total Items,Total Amount,Status,Created By,Order Date,Expected Delivery Date\n");

        List<PurchaseOrder> orders = purchaseOrderRepository.findAll();
        for (PurchaseOrder po : orders) {
            csv.append(po.getId()).append(",")
                    .append(escape(po.getOrderNumber())).append(",")
                    .append(escape(po.getSupplier() != null ? po.getSupplier().getName() : "")).append(",")
                    .append(po.getItems() != null ? po.getItems().size() : 0).append(",")
                    .append(po.getTotalAmount() != null ? po.getTotalAmount() : "0.00").append(",")
                    .append(po.getStatus() != null ? po.getStatus().name() : "").append(",")
                    .append(escape(po.getCreatedBy())).append(",")
                    .append(escape(po.getCreatedAt() != null ? po.getCreatedAt().toString() : "")).append(",")
                    .append(escape(po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().toString() : "")).append("\n");
        }
        return csv.toString();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public String exportSalesOrdersCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Sales Orders CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("Order ID,Order Number,Customer Name,Customer Email,Total Items,Total Amount,Status,Created By,Order Date\n");

        List<SalesOrder> orders = salesOrderRepository.findAll();
        for (SalesOrder so : orders) {
            csv.append(so.getId()).append(",")
                    .append(escape(so.getOrderNumber())).append(",")
                    .append(escape(so.getCustomerName())).append(",")
                    .append(escape(so.getCustomerEmail())).append(",")
                    .append(so.getItems() != null ? so.getItems().size() : 0).append(",")
                    .append(so.getTotalAmount() != null ? so.getTotalAmount() : "0.00").append(",")
                    .append(so.getStatus() != null ? so.getStatus().name() : "").append(",")
                    .append(escape(so.getCreatedBy())).append(",")
                    .append(escape(so.getCreatedAt() != null ? so.getCreatedAt().toString() : "")).append("\n");
        }
        return csv.toString();
    }

    public String exportReturnsCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Returns CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("Return ID,Return Number,Return Type,Reference Order,Reason,Status,Created By,Date\n");

        List<ReturnOrder> returns = returnOrderRepository.findAll();
        for (ReturnOrder ro : returns) {
            csv.append(ro.getId()).append(",")
                    .append(escape(ro.getReturnNumber())).append(",")
                    .append(escape(ro.getReturnType() != null ? ro.getReturnType().name() : "")).append(",")
                    .append(escape(ro.getReferenceOrderNumber())).append(",")
                    .append(escape(ro.getReason())).append(",")
                    .append(escape(ro.getStatus() != null ? ro.getStatus().name() : "")).append(",")
                    .append(escape(ro.getCreatedBy())).append(",")
                    .append(escape(ro.getCreatedAt() != null ? ro.getCreatedAt().toString() : "")).append("\n");
        }
        return csv.toString();
    }

    public String exportUsersCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Users CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("User ID,Name,Email,Role,Status,Created Date,Last Login\n");

        List<User> users = userRepository.findAll();
        for (User u : users) {
            csv.append(u.getId()).append(",")
                    .append(escape(u.getName())).append(",")
                    .append(escape(u.getEmail())).append(",")
                    .append(escape(u.getRole() != null ? u.getRole().name() : "")).append(",")
                    .append(escape(u.getStatus() != null ? u.getStatus().name() : "")).append(",")
                    .append(escape(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "")).append(",")
                    .append(escape(u.getLastLogin() != null ? u.getLastLogin().toString() : "Never")).append("\n");
        }
        return csv.toString();
    }

    public String exportStockMovementsCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Stock Movements CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("Movement ID,Product,Movement Type,Quantity,Previous Stock,Resulting Stock,Reference,Performed By,Date\n");

        List<StockMovement> movements = stockMovementRepository.findAllByOrderByTimestampDesc();
        for (StockMovement sm : movements) {
            csv.append(sm.getId()).append(",")
                    .append(escape(sm.getProduct() != null ? sm.getProduct().getName() : "")).append(",")
                    .append(escape(sm.getMovementType() != null ? sm.getMovementType().name() : "")).append(",")
                    .append(sm.getQuantity()).append(",")
                    .append(sm.getPreviousStock()).append(",")
                    .append(sm.getResultingStock()).append(",")
                    .append(escape(sm.getReferenceOrder())).append(",")
                    .append(escape(sm.getPerformedBy())).append(",")
                    .append(escape(sm.getTimestamp() != null ? sm.getTimestamp().toString() : "")).append("\n");
        }
        return csv.toString();
    }

    public String exportActivityLogsCsv() {
        activityLogService.log("EXPORT", "REPORTS", "Exported Activity Logs CSV");
        StringBuilder csv = new StringBuilder();
        csv.append("Log ID,User Email,User Name,Action,Module,Description,Timestamp\n");

        List<ActivityLog> logs = activityLogRepository.findAllByOrderByTimestampDesc();
        for (ActivityLog al : logs) {
            csv.append(al.getId()).append(",")
                    .append(escape(al.getUserEmail())).append(",")
                    .append(escape(al.getUserName())).append(",")
                    .append(escape(al.getAction())).append(",")
                    .append(escape(al.getModule())).append(",")
                    .append(escape(al.getDescription())).append(",")
                    .append(escape(al.getTimestamp() != null ? al.getTimestamp().toString() : "")).append("\n");
        }
        return csv.toString();
    }

    public DeadStockReportResponse getDeadStockReport(int slowDays, int deadDays) {
        if (slowDays <= 0) slowDays = 30;
        if (deadDays <= slowDays) deadDays = Math.max(60, slowDays + 30);

        List<Product> products = productRepository.findAll();
        List<DeadStockItemResponse> itemResponses = new ArrayList<>();

        int totalProductsAnalyzed = 0;
        int deadStockCount = 0;
        int slowMovingCount = 0;
        int activeCount = 0;
        int newUnsoldCount = 0;

        BigDecimal totalDeadStockValue = BigDecimal.ZERO;
        BigDecimal totalSlowMovingValue = BigDecimal.ZERO;
        BigDecimal totalCapitalTiedUp = BigDecimal.ZERO;

        LocalDateTime now = LocalDateTime.now();

        for (Product p : products) {
            // Edge Case: Do NOT classify products with zero current stock as dead stock
            if (p.getQuantity() == null || p.getQuantity() <= 0) {
                continue;
            }

            totalProductsAnalyzed++;

            // Find last sale date from StockMovement or SalesOrderItem
            LocalDateTime lastStockSale = stockMovementRepository.findLastSaleTimestampByProductId(p.getId());
            LocalDateTime lastOrderSale = salesOrderRepository.findLastSalesOrderDateByProductId(p.getId());

            LocalDateTime lastSaleDate = null;
            if (lastStockSale != null && lastOrderSale != null) {
                lastSaleDate = lastStockSale.isAfter(lastOrderSale) ? lastStockSale : lastOrderSale;
            } else if (lastStockSale != null) {
                lastSaleDate = lastStockSale;
            } else if (lastOrderSale != null) {
                lastSaleDate = lastOrderSale;
            }

            LocalDateTime createdDate = p.getCreatedAt() != null ? p.getCreatedAt() : now;
            long productAgeDays = java.time.temporal.ChronoUnit.DAYS.between(createdDate, now);

            Long daysWithoutSale = null;
            String status;
            String statusLabel;
            String recommendedAction;

            if (lastSaleDate != null) {
                daysWithoutSale = java.time.temporal.ChronoUnit.DAYS.between(lastSaleDate, now);
                if (daysWithoutSale >= deadDays) {
                    status = "DEAD_STOCK";
                    statusLabel = "Dead Stock (" + daysWithoutSale + "d unsold)";
                    recommendedAction = "Immediate clearance sale (30-50% discount), bundle promotion, or vendor return.";
                    deadStockCount++;
                } else if (daysWithoutSale >= slowDays) {
                    status = "SLOW_MOVING";
                    statusLabel = "Slow Moving (" + daysWithoutSale + "d unsold)";
                    recommendedAction = "Consider 10-20% discount, targeted marketing, or prominent shelf display.";
                    slowMovingCount++;
                } else {
                    status = "ACTIVE";
                    statusLabel = "Active Stock";
                    recommendedAction = "Stock is moving normally. Maintain standard inventory replenishment.";
                    activeCount++;
                }
            } else {
                // Product has never been sold
                if (productAgeDays < slowDays) {
                    status = "NEW_UNSOLD";
                    statusLabel = "New Item (Unsold)";
                    recommendedAction = "Newly added catalog item. Monitor initial customer demand.";
                    newUnsoldCount++;
                } else if (productAgeDays >= deadDays) {
                    status = "DEAD_STOCK";
                    statusLabel = "Dead Stock (Never Sold - " + productAgeDays + "d old)";
                    recommendedAction = "Product has never sold since added " + productAgeDays + " days ago. Liquidate or discount.";
                    deadStockCount++;
                } else {
                    status = "SLOW_MOVING";
                    statusLabel = "Slow Moving (Never Sold - " + productAgeDays + "d old)";
                    recommendedAction = "No sales recorded since added " + productAgeDays + " days ago. Promote or discount.";
                    slowMovingCount++;
                }
            }

            BigDecimal unitPrice = p.getPurchasePrice() != null && p.getPurchasePrice().compareTo(BigDecimal.ZERO) > 0
                    ? p.getPurchasePrice()
                    : (p.getSellingPrice() != null ? p.getSellingPrice() : BigDecimal.ZERO);
            BigDecimal stockValue = unitPrice.multiply(BigDecimal.valueOf(p.getQuantity()));

            if ("DEAD_STOCK".equals(status)) {
                totalDeadStockValue = totalDeadStockValue.add(stockValue);
                totalCapitalTiedUp = totalCapitalTiedUp.add(stockValue);
            } else if ("SLOW_MOVING".equals(status)) {
                totalSlowMovingValue = totalSlowMovingValue.add(stockValue);
                totalCapitalTiedUp = totalCapitalTiedUp.add(stockValue);
            } else if ("NEW_UNSOLD".equals(status)) {
                totalCapitalTiedUp = totalCapitalTiedUp.add(stockValue);
            }

            DeadStockItemResponse item = new DeadStockItemResponse(
                    p.getId(),
                    p.getName(),
                    p.getSku() != null ? p.getSku() : "SKU-" + p.getId(),
                    p.getCategory() != null ? p.getCategory().getName() : "General",
                    p.getSupplier() != null ? p.getSupplier().getName() : "—",
                    p.getQuantity(),
                    p.getPurchasePrice() != null ? p.getPurchasePrice() : BigDecimal.ZERO,
                    p.getSellingPrice() != null ? p.getSellingPrice() : BigDecimal.ZERO,
                    stockValue,
                    lastSaleDate,
                    daysWithoutSale,
                    productAgeDays,
                    status,
                    statusLabel,
                    recommendedAction
            );
            itemResponses.add(item);
        }

        // Sort items: DEAD_STOCK first, then SLOW_MOVING, then NEW_UNSOLD, then ACTIVE, ordered by days descending
        itemResponses.sort((a, b) -> {
            int priorityA = getStatusPriority(a.getStatus());
            int priorityB = getStatusPriority(b.getStatus());
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            long compareA = a.getDaysWithoutSale() != null ? a.getDaysWithoutSale() : (a.getProductAgeDays() != null ? a.getProductAgeDays() : 0);
            long compareB = b.getDaysWithoutSale() != null ? b.getDaysWithoutSale() : (b.getProductAgeDays() != null ? b.getProductAgeDays() : 0);
            return Long.compare(compareB, compareA);
        });

        return new DeadStockReportResponse(
                totalProductsAnalyzed,
                deadStockCount,
                slowMovingCount,
                activeCount,
                newUnsoldCount,
                totalDeadStockValue,
                totalSlowMovingValue,
                totalCapitalTiedUp,
                slowDays,
                deadDays,
                itemResponses
        );
    }

    private int getStatusPriority(String status) {
        if ("DEAD_STOCK".equals(status)) return 1;
        if ("SLOW_MOVING".equals(status)) return 2;
        if ("NEW_UNSOLD".equals(status)) return 3;
        return 4; // ACTIVE
    }

    public String exportDeadStockCsv(int slowDays, int deadDays) {
        activityLogService.log("EXPORT", "REPORTS", "Exported Dead Stock Analysis CSV (Slow: " + slowDays + "d, Dead: " + deadDays + "d)");
        DeadStockReportResponse report = getDeadStockReport(slowDays, deadDays);

        StringBuilder csv = new StringBuilder();
        csv.append("Product ID,Product Name,SKU,Category,Supplier,Current Stock,Purchase Price,Selling Price,Estimated Capital Value,Last Sale Date,Days Without Sale,Status,Recommended Action\n");

        for (DeadStockItemResponse item : report.getItems()) {
            csv.append(item.getId()).append(",")
                    .append(escape(item.getName())).append(",")
                    .append(escape(item.getSku())).append(",")
                    .append(escape(item.getCategory())).append(",")
                    .append(escape(item.getSupplier())).append(",")
                    .append(item.getCurrentStock()).append(",")
                    .append(item.getPurchasePrice()).append(",")
                    .append(item.getSellingPrice()).append(",")
                    .append(item.getEstimatedStockValue()).append(",")
                    .append(escape(item.getLastSaleDate() != null ? item.getLastSaleDate().toString() : "Never Sold")).append(",")
                    .append(item.getDaysWithoutSale() != null ? item.getDaysWithoutSale() : "N/A").append(",")
                    .append(escape(item.getStatusLabel())).append(",")
                    .append(escape(item.getRecommendedAction())).append("\n");
        }
        return csv.toString();
    }

    private String escape(String val) {
        if (val == null) return "";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }
}
