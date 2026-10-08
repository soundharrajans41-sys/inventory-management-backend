package com.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.LoginRequest;
import com.inventory.dto.ProductRequest;
import com.inventory.dto.SignupRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationAndSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtainToken(String email, String password) throws Exception {
        LoginRequest login = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("token").asText();
    }

    @Test
    @DisplayName("Valid login returns JWT and 200 OK")
    void testValidLogin() throws Exception {
        LoginRequest login = new LoginRequest("admin@inventory.com", "admin123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Invalid password returns 401 Unauthorized")
    void testInvalidPassword() throws Exception {
        LoginRequest login = new LoginRequest("admin@inventory.com", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated access to protected endpoint returns 401")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Viewer cannot create products (403 Forbidden)")
    void testViewerForbiddenOnPostProduct() throws Exception {
        String viewerToken = obtainToken("viewer@inventory.com", "viewer123");

        ProductRequest newProduct = new ProductRequest();
        newProduct.setName("Test Product By Viewer");
        newProduct.setStock(10);
        newProduct.setPrice(new BigDecimal("29.99"));

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Staff cannot manage users (403 Forbidden)")
    void testStaffForbiddenOnUsersEndpoint() throws Exception {
        String staffToken = obtainToken("staff@inventory.com", "staff123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin can view users list (200 OK)")
    void testAdminAllowedOnUsersEndpoint() throws Exception {
        String adminToken = obtainToken("admin@inventory.com", "admin123");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
