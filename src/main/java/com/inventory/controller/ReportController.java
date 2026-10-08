package com.inventory.controller;

import com.inventory.dto.FinancialReportResponse;
import com.inventory.dto.InventoryReportResponse;
import com.inventory.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reporting & Analytics", description = "Endpoints for inventory summaries, financial performance, and CSV exports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/financial")
    @Operation(summary = "Get financial performance summary (revenue, cost, net profit)")
    public ResponseEntity<FinancialReportResponse> getFinancialReport() {
        return ResponseEntity.ok(reportService.getFinancialReport());
    }

    @GetMapping("/inventory")
    @Operation(summary = "Get inventory health and category distribution report")
    public ResponseEntity<InventoryReportResponse> getInventoryReport() {
        return ResponseEntity.ok(reportService.getInventoryReport());
    }

    @GetMapping("/dead-stock")
    @Operation(summary = "Get Dead Stock analysis identifying stagnant inventory and tied-up capital")
    public ResponseEntity<com.inventory.dto.DeadStockReportResponse> getDeadStockReport(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "30") int slowDays,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "60") int deadDays) {
        return ResponseEntity.ok(reportService.getDeadStockReport(slowDays, deadDays));
    }

    @GetMapping(value = "/export-csv", produces = "text/csv")
    @Operation(summary = "Export current inventory catalog as CSV")
    public ResponseEntity<String> exportCsv() {
        String csv = reportService.exportReportsCsv();
        return createCsvResponse("inventory_report.csv", csv);
    }

    @GetMapping(value = "/export/{type}", produces = "text/csv")
    @Operation(summary = "Export specific dataset as CSV (products, suppliers, purchases, sales, returns, users, stock-movements, activity-logs, dead-stock)")
    public ResponseEntity<String> exportDataset(
            @PathVariable String type,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "30") int slowDays,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "60") int deadDays) {
        String filename;
        String csv;

        switch (type.toLowerCase()) {
            case "dead-stock":
            case "deadstock":
                filename = "dead_stock_analysis.csv";
                csv = reportService.exportDeadStockCsv(slowDays, deadDays);
                break;
            case "products":
            case "inventory":
                filename = "products_export.csv";
                csv = reportService.exportProductsCsv();
                break;
            case "suppliers":
                filename = "suppliers_export.csv";
                csv = reportService.exportSuppliersCsv();
                break;
            case "purchases":
            case "purchase-orders":
                filename = "purchase_orders_export.csv";
                csv = reportService.exportPurchaseOrdersCsv();
                break;
            case "sales":
            case "sales-orders":
                filename = "sales_orders_export.csv";
                csv = reportService.exportSalesOrdersCsv();
                break;
            case "returns":
                filename = "returns_export.csv";
                csv = reportService.exportReturnsCsv();
                break;
            case "users":
                filename = "users_export.csv";
                csv = reportService.exportUsersCsv();
                break;
            case "stock":
            case "stock-movements":
                filename = "stock_movements_export.csv";
                csv = reportService.exportStockMovementsCsv();
                break;
            case "activity":
            case "activity-logs":
                filename = "activity_logs_export.csv";
                csv = reportService.exportActivityLogsCsv();
                break;
            default:
                filename = "inventory_report.csv";
                csv = reportService.exportProductsCsv();
        }

        return createCsvResponse(filename, csv);
    }

    private ResponseEntity<String> createCsvResponse(String filename, String csv) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
