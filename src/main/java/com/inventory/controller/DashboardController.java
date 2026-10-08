package com.inventory.controller;

import com.inventory.dto.DashboardOverviewResponse;
import com.inventory.dto.StaffDashboardResponse;
import com.inventory.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Endpoints for real-time dashboard analytics, widgets, and KPI summaries")
public class DashboardController {

    private final DashboardService dashboardService;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get admin dashboard overview metrics (real values from MySQL)")
    public ResponseEntity<DashboardOverviewResponse> getOverview() {
        return ResponseEntity.ok(dashboardService.getOverview());
    }

    @GetMapping("/staff")
    @Operation(summary = "Get staff dashboard metrics")
    public ResponseEntity<StaffDashboardResponse> getStaffDashboard() {
        return ResponseEntity.ok(dashboardService.getStaffDashboard());
    }

    @org.springframework.web.bind.annotation.PostMapping("/seed-dead-stock")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> seedDeadStock() {
        // Create an old Dead Stock product
        em.createNativeQuery("INSERT INTO products (name, sku, quantity, minimum_stock_level, purchase_price, selling_price, status, created_at, updated_at) " +
                             "VALUES ('Vintage Typewriter', 'SKU-VINTAGE-1', 15, 5, 2000.00, 3500.00, 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 95 DAY), DATE_SUB(NOW(), INTERVAL 95 DAY))")
          .executeUpdate();
          
        // Create an old Slow Moving product
        em.createNativeQuery("INSERT INTO products (name, sku, quantity, minimum_stock_level, purchase_price, selling_price, status, created_at, updated_at) " +
                             "VALUES ('Retro Telephone', 'SKU-RETRO-1', 40, 10, 1500.00, 2500.00, 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 45 DAY), DATE_SUB(NOW(), INTERVAL 45 DAY))")
          .executeUpdate();

        // Create an old active product that had a recent sale
        em.createNativeQuery("INSERT INTO products (name, sku, quantity, minimum_stock_level, purchase_price, selling_price, status, created_at, updated_at) " +
                             "VALUES ('Active Watch', 'SKU-WATCH-1', 50, 10, 5000.00, 8000.00, 'AVAILABLE', DATE_SUB(NOW(), INTERVAL 120 DAY), NOW())")
          .executeUpdate();
          
        Long activeWatchId = ((Number) em.createNativeQuery("SELECT id FROM products WHERE sku = 'SKU-WATCH-1'").getSingleResult()).longValue();
        
        // Add a recent sale for Active Watch
        em.createNativeQuery("INSERT INTO stock_movements (product_id, quantity, previous_stock, resulting_stock, movement_type, reference_order, performed_by, timestamp) " +
                             "VALUES (?, 2, 52, 50, 'SALE', 'ORD-WATCH-1', 'admin', DATE_SUB(NOW(), INTERVAL 5 DAY))")
          .setParameter(1, activeWatchId)
          .executeUpdate();

        return ResponseEntity.ok("Successfully seeded dead stock and slow moving test products.");
    }
}
