package com.zestindia.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zestindia.productservice.dto.request.ItemCreateRequest;
import com.zestindia.productservice.dto.request.ProductCreateRequest;
import com.zestindia.productservice.dto.request.ProductUpdateRequest;
import com.zestindia.productservice.entity.Item;
import com.zestindia.productservice.entity.Product;
import com.zestindia.productservice.repository.ItemRepository;
import com.zestindia.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        productRepository.deleteAll();

        Product product = new Product("Initial Test Product");
        testProduct = productRepository.save(product);
    }

    @Test
    @DisplayName("GET /api/v1/products: Returns paginated products list")
    void testGetAllProducts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id}: Returns product details when found")
    void testGetProductById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.productName").value("Initial Test Product"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id}: Returns 404 when product not found")
    void testGetProductById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", 9999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/products: Successfully creates product when authenticated as ADMIN")
    void testCreateProduct_AuthenticatedAdmin() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest("Sony WH-1000XM5");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Sony WH-1000XM5"))
                .andExpect(jsonPath("$.data.createdBy").value("admin"));
    }

    @Test
    @DisplayName("POST /api/v1/products: Rejects unauthenticated request with 401 Unauthorized")
    void testCreateProduct_Unauthenticated() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest("Sony WH-1000XM5");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("POST /api/v1/products: Returns 400 when product name is blank")
    void testCreateProduct_ValidationFailure() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest("");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.productName").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("PUT /api/v1/products/{id}: Successfully updates product")
    void testUpdateProduct_Success() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest("Updated Name V2");

        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productName").value("Updated Name V2"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("DELETE /api/v1/products/{id}: Successfully deletes product when role is ADMIN")
    void testDeleteProduct_AdminSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("DELETE /api/v1/products/{id}: Rejects deletion with 403 Forbidden for non-admin USER")
    void testDeleteProduct_ForbiddenForUser() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/products/{id}/items: Successfully adds item to product")
    void testAddItemToProduct_Success() throws Exception {
        ItemCreateRequest request = new ItemCreateRequest(100);

        mockMvc.perform(post("/api/v1/products/{id}/items", testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.quantity").value(100))
                .andExpect(jsonPath("$.data.productId").value(testProduct.getId()));
    }
}
