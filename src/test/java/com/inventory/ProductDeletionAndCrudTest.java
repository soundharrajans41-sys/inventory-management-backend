package com.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.LoginRequest;
import com.inventory.dto.ProductRequest;
import com.inventory.dto.ProductResponse;
import com.inventory.entity.Product;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductDeletionAndCrudTest {

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
    @DisplayName("Referenced product deletion returns 409 Conflict without modifying stock history")
    void testReferencedProductDeletionReturnsConflict() throws Exception {
        String token = obtainAdminToken();

        // Product ID 1 (Wireless Headphones) has stock movement history in database
        Long productId = 1L;
        assertTrue(productRepository.existsById(productId), "Product 1 should exist");
        long initialMovements = stockMovementRepository.countByProductId(productId);
        assertTrue(initialMovements > 0, "Product 1 must have stock movements to test reference protection");

        // Attempt deletion
        mockMvc.perform(delete("/api/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Cannot delete product")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("stock movement(s)")));

        // Verify product 1 is still present in database
        assertTrue(productRepository.existsById(productId), "Product 1 must remain intact");

        // Verify stock movements count has not decreased or been deleted
        assertEquals(initialMovements, stockMovementRepository.countByProductId(productId),
                "Stock movement history must remain untouched");
    }

    @Test
    @DisplayName("Unreferenced product can be created, updated, and deleted cleanly (204 No Content)")
    void testUnreferencedProductCrudAndDeletion() throws Exception {
        String token = obtainAdminToken();

        // 1. Create an unreferenced product (0 stock, no movements created)
        ProductRequest req = new ProductRequest();
        req.setName("Clean Deletable Item " + System.currentTimeMillis());
        req.setStock(0);
        req.setPrice(new BigDecimal("99.99"));
        req.setDescription("Item without any stock movements or orders");

        MvcResult createResult = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(req.getName()))
                .andReturn();

        ProductResponse created = objectMapper.readValue(createResult.getResponse().getContentAsString(), ProductResponse.class);
        Long newId = created.getId();
        assertNotNull(newId);

        // Verify 0 stock movements exist for this item
        assertEquals(0, stockMovementRepository.countByProductId(newId));

        // 2. Update the product
        ProductRequest updateReq = new ProductRequest();
        updateReq.setName(req.getName() + " Updated");
        updateReq.setDescription("Updated description");
        updateReq.setPrice(new BigDecimal("129.99"));

        mockMvc.perform(put("/api/products/" + newId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateReq.getName()))
                .andExpect(jsonPath("$.sellingPrice").value(129.99));

        // 3. Delete the unreferenced product
        mockMvc.perform(delete("/api/products/" + newId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // 4. Verify product no longer exists in database
        assertFalse(productRepository.existsById(newId), "Product should be permanently deleted");
    }

    @Test
    @DisplayName("Deleting non-existent product returns 404 Not Found")
    void testDeleteNonExistentProduct() throws Exception {
        String token = obtainAdminToken();

        mockMvc.perform(delete("/api/products/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
