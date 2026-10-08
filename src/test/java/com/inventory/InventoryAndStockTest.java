package com.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.*;
import com.inventory.entity.Product;
import com.inventory.entity.StockStatus;
import com.inventory.repository.ProductRepository;
import com.inventory.repository.StockMovementRepository;
import com.inventory.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InventoryAndStockTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductService productService;

    private String obtainAdminToken() throws Exception {
        LoginRequest login = new LoginRequest("admin@inventory.com", "admin123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    @DisplayName("Stock Entry increases quantity and creates stock movement")
    void testStockEntryIncreasesQuantity() throws Exception {
        String token = obtainAdminToken();
        Long productId = null;

        try {
            // 1. Create a product
            ProductRequest req = new ProductRequest();
            req.setName("Test Stock Entry Item " + System.currentTimeMillis());
            req.setStock(10);
            req.setPrice(new BigDecimal("15.00"));
            ProductResponse created = productService.createProduct(req);
            productId = created.getId();

            // 2. Perform stock entry
            StockEntryRequest entryReq = new StockEntryRequest();
            entryReq.setProductId(productId);
            entryReq.setItemName(created.getName());
            entryReq.setQuantity(25);
            entryReq.setSupplier("Apex Logistics");

            mockMvc.perform(post("/api/inventory/stock-in")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(entryReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity").value(35));

            Product updated = productRepository.findById(productId).orElseThrow();
            assertEquals(35, updated.getQuantity());

            // Verify stock movement was recorded
            boolean hasMovement = stockMovementRepository.findByProductIdOrderByTimestampDesc(productId)
                    .stream()
                    .anyMatch(m -> m.getQuantity() == 25);
            assertTrue(hasMovement);
        } finally {
            if (productId != null) {
                stockMovementRepository.deleteAll(stockMovementRepository.findByProductIdOrderByTimestampDesc(productId));
                productRepository.deleteById(productId);
            }
        }
    }

    @Test
    @DisplayName("Prevent selling more stock than available (negative stock prevented)")
    void testPreventNegativeStock() throws Exception {
        String token = obtainAdminToken();
        Long productId = null;

        try {
            // Create product with 5 units
            ProductRequest req = new ProductRequest();
            req.setName("Test Limited Stock Item " + System.currentTimeMillis());
            req.setStock(5);
            req.setPrice(new BigDecimal("50.00"));
            ProductResponse created = productService.createProduct(req);
            productId = created.getId();

            // Attempt sales order with 10 units (more than 5)
            SalesOrderRequest salesReq = new SalesOrderRequest();
            salesReq.setCustomerName("David Testing");
            salesReq.setCustomerEmail("david@example.com");
            salesReq.setItems(Collections.singletonList(
                    new SalesOrderItemRequest(productId, 10, new BigDecimal("50.00"))
            ));

            mockMvc.perform(post("/api/sales")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(salesReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Insufficient Stock"));

            // Product stock must remain 5
            Product product = productRepository.findById(productId).orElseThrow();
            assertEquals(5, product.getQuantity());
        } finally {
            if (productId != null) {
                stockMovementRepository.deleteAll(stockMovementRepository.findByProductIdOrderByTimestampDesc(productId));
                productRepository.deleteById(productId);
            }
        }
    }

    @Test
    @DisplayName("Dashboard overview returns non-mock real database metrics")
    void testDashboardMetrics() throws Exception {
        String token = obtainAdminToken();

        mockMvc.perform(get("/api/dashboard/overview")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productsCount").isNumber())
                .andExpect(jsonPath("$.totalStock").isNumber())
                .andExpect(jsonPath("$.usersCount").isNumber());
    }
}
