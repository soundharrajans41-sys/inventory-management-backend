package com.inventory.service;

import com.inventory.dto.DashboardOverviewResponse;
import com.inventory.dto.StaffDashboardResponse;
import com.inventory.entity.*;
import com.inventory.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final AlertRepository alertRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementService stockMovementService;
    private final ProductService productService;
    private final ActivityLogService activityLogService;

    public DashboardService(ProductRepository productRepository,
                            CategoryRepository categoryRepository,
                            SupplierRepository supplierRepository,
                            UserRepository userRepository,
                            AlertRepository alertRepository,
                            PurchaseOrderRepository purchaseOrderRepository,
                            SalesOrderRepository salesOrderRepository,
                            StockMovementRepository stockMovementRepository,
                            StockMovementService stockMovementService,
                            ProductService productService,
                            ActivityLogService activityLogService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.alertRepository = alertRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementService = stockMovementService;
        this.productService = productService;
        this.activityLogService = activityLogService;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview() {
        DashboardOverviewResponse response = new DashboardOverviewResponse();

        BigDecimal totalVal = productRepository.calculateTotalInventoryValue();
        response.setTotalValue(totalVal != null ? totalVal : BigDecimal.ZERO);
        response.setProductsCount(productRepository.count());
        response.setUsersCount(userRepository.count());
        response.setAlertsCount(alertRepository.countByIsReadFalse());

        Long totalStock = productRepository.calculateTotalStock();
        response.setTotalStock(totalStock != null ? totalStock : 0);
        response.setLowStockCount(productRepository.countByStatus(StockStatus.LOW_STOCK));
        response.setOutOfStockCount(productRepository.countByStatus(StockStatus.OUT_OF_STOCK));

        response.setSuppliersCount(supplierRepository.count());
        response.setPendingPurchases(purchaseOrderRepository.countByStatus(PurchaseStatus.PENDING));
        response.setPendingSales(salesOrderRepository.countByStatus(SalesStatus.PENDING));
        response.setTotalSalesOrdersCount(salesOrderRepository.count());
        response.setTotalPurchaseOrdersCount(purchaseOrderRepository.count());

        response.setTotalSalesValue(salesOrderRepository.calculateTotalSalesValue());
        response.setTotalPurchasesValue(purchaseOrderRepository.calculateTotalPurchasesValue());

        response.setRecentMovements(stockMovementService.getRecentMovements());
        response.setLowStockProducts(
                productRepository.findByStatus(StockStatus.LOW_STOCK).stream()
                        .map(productService::mapToResponse)
                        .toList()
        );

        // 1. Category Stock Distribution
        List<DashboardOverviewResponse.CategoryStockSummary> categoryStockList = new ArrayList<>();
        List<Category> categories = categoryRepository.findAll();
        for (Category cat : categories) {
            List<Product> prods = productRepository.findByCategoryId(cat.getId());
            long count = prods != null ? prods.size() : 0;
            long stock = prods != null ? prods.stream().mapToLong(Product::getQuantity).sum() : 0;
            categoryStockList.add(new DashboardOverviewResponse.CategoryStockSummary(cat.getName(), count, stock));
        }
        response.setCategoryStock(categoryStockList);

        // 2. Monthly Sales & Purchases (Past 6 Months)
        List<DashboardOverviewResponse.MonthlySalesSummary> monthlySalesList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<SalesOrder> allSales = salesOrderRepository.findAll();
        List<PurchaseOrder> allPurchases = purchaseOrderRepository.findAll();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            int year = monthDate.getYear();
            int monthVal = monthDate.getMonthValue();
            String monthLabel = monthDate.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            BigDecimal monthSalesTotal = BigDecimal.ZERO;
            long monthOrderCount = 0;
            for (SalesOrder s : allSales) {
                if (s.getCreatedAt() != null &&
                        s.getCreatedAt().getYear() == year &&
                        s.getCreatedAt().getMonthValue() == monthVal &&
                        s.getStatus() != SalesStatus.CANCELLED) {
                    if (s.getTotalAmount() != null) {
                        monthSalesTotal = monthSalesTotal.add(s.getTotalAmount());
                    }
                    monthOrderCount++;
                }
            }

            BigDecimal monthPurchasesTotal = BigDecimal.ZERO;
            for (PurchaseOrder p : allPurchases) {
                if (p.getCreatedAt() != null &&
                        p.getCreatedAt().getYear() == year &&
                        p.getCreatedAt().getMonthValue() == monthVal &&
                        p.getStatus() == PurchaseStatus.RECEIVED) {
                    if (p.getTotalAmount() != null) {
                        monthPurchasesTotal = monthPurchasesTotal.add(p.getTotalAmount());
                    }
                }
            }

            monthlySalesList.add(new DashboardOverviewResponse.MonthlySalesSummary(
                    monthLabel, monthSalesTotal, monthPurchasesTotal, monthOrderCount
            ));
        }
        response.setMonthlySales(monthlySalesList);

        // 3. Top Selling Products
        Map<String, long[]> productSalesAgg = new HashMap<>(); // product name -> [quantity, revenueLong]
        for (SalesOrder s : allSales) {
            if (s.getStatus() != SalesStatus.CANCELLED && s.getItems() != null) {
                for (SalesOrderItem item : s.getItems()) {
                    if (item.getProduct() != null) {
                        String pName = item.getProduct().getName();
                        long qty = item.getQuantity();
                        long rev = item.getSubtotal() != null ? item.getSubtotal().longValue() : 0;
                        productSalesAgg.compute(pName, (k, v) -> v == null ? new long[]{qty, rev} : new long[]{v[0] + qty, v[1] + rev});
                    }
                }
            }
        }

        List<DashboardOverviewResponse.TopSellingProductSummary> topSellingList = productSalesAgg.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue()[0], e1.getValue()[0]))
                .limit(5)
                .map(e -> new DashboardOverviewResponse.TopSellingProductSummary(
                        e.getKey(),
                        e.getValue()[0],
                        BigDecimal.valueOf(e.getValue()[1])
                ))
                .collect(Collectors.toList());
        response.setTopSellingProducts(topSellingList);

        // 4. Stock Movement Trends (Past 7 Days)
        List<DashboardOverviewResponse.StockMovementTrendSummary> movementTrends = new ArrayList<>();
        List<StockMovement> allMovements = stockMovementRepository.findAllByOrderByTimestampDesc();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            String dateLabel = date.format(DateTimeFormatter.ofPattern("MMM dd"));
            long incoming = 0;
            long outgoing = 0;

            for (StockMovement m : allMovements) {
                if (m.getTimestamp() != null && m.getTimestamp().toLocalDate().isEqual(date)) {
                    if (m.getMovementType() == MovementType.PURCHASE || m.getMovementType() == MovementType.RETURN_IN) {
                        incoming += m.getQuantity();
                    } else {
                        outgoing += m.getQuantity();
                    }
                }
            }
            movementTrends.add(new DashboardOverviewResponse.StockMovementTrendSummary(dateLabel, incoming, outgoing));
        }
        response.setStockMovementTrends(movementTrends);

        // 5. Recent Activity Logs
        response.setRecentActivities(activityLogService.getRecentLogs());

        return response;
    }

    public StaffDashboardResponse getStaffDashboard() {
        long recentMovements = stockMovementService.getAllMovements().stream()
                .filter(m -> m.getTimestamp() != null && m.getTimestamp().isAfter(LocalDateTime.now().minusDays(7)))
                .count();

        long ordersProcessed = salesOrderRepository.countByStatus(SalesStatus.PROCESSING)
                + salesOrderRepository.countByStatus(SalesStatus.SHIPPED)
                + salesOrderRepository.countByStatus(SalesStatus.DELIVERED);

        long pendingTasks = salesOrderRepository.countByStatus(SalesStatus.PENDING)
                + purchaseOrderRepository.countByStatus(PurchaseStatus.PENDING);

        return new StaffDashboardResponse(
                recentMovements,
                ordersProcessed,
                14,
                pendingTasks
        );
    }
}
