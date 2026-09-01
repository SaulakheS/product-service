package com.zestindia.productservice.service;

import com.zestindia.productservice.dto.request.ItemCreateRequest;
import com.zestindia.productservice.dto.request.ProductCreateRequest;
import com.zestindia.productservice.dto.request.ProductUpdateRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.ItemResponse;
import com.zestindia.productservice.dto.response.PagedResponse;
import com.zestindia.productservice.dto.response.ProductResponse;
import com.zestindia.productservice.entity.Item;
import com.zestindia.productservice.entity.Product;
import com.zestindia.productservice.event.ProductActivityEvent;
import com.zestindia.productservice.exception.ResourceNotFoundException;
import com.zestindia.productservice.repository.ItemRepository;
import com.zestindia.productservice.repository.ProductRepository;
import com.zestindia.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("MacBook Pro M3");
        sampleProduct.setId(1);
        sampleProduct.setCreatedBy("admin");
        sampleProduct.setCreatedOn(LocalDateTime.now());
    }

    @Test
    @DisplayName("Create Product: should persist and return ProductResponse")
    void testCreateProduct_Success() {
        ProductCreateRequest request = new ProductCreateRequest("MacBook Pro M3");
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getProductName()).isEqualTo("MacBook Pro M3");
        assertThat(response.getCreatedBy()).isEqualTo("admin");

        verify(productRepository, times(1)).save(any(Product.class));
        verify(eventPublisher, times(1)).publishEvent(any(ProductActivityEvent.class));
    }

    @Test
    @DisplayName("Get Product by ID: should return product if found")
    void testGetProductById_Success() {
        when(productRepository.findByIdWithItems(1)).thenReturn(Optional.of(sampleProduct));

        ProductResponse response = productService.getProductById(1);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getProductName()).isEqualTo("MacBook Pro M3");
        verify(productRepository, times(1)).findByIdWithItems(1);
    }

    @Test
    @DisplayName("Get Product by ID: should throw ResourceNotFoundException if not found")
    void testGetProductById_NotFound() {
        when(productRepository.findByIdWithItems(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: '99'");

        verify(productRepository, times(1)).findByIdWithItems(99);
    }

    @Test
    @DisplayName("Get All Products: should return paginated response")
    void testGetAllProducts_Success() {
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), PageRequest.of(0, 10, Sort.by("id").ascending()), 1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> response = productService.getAllProducts(0, 10, "id", "asc", null);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getPageNo()).isEqualTo(0);
        assertThat(response.isLast()).isTrue();
    }

    @Test
    @DisplayName("Update Product: should update and return modified product")
    void testUpdateProduct_Success() {
        ProductUpdateRequest request = new ProductUpdateRequest("MacBook Pro M3 Max");
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponse response = productService.updateProduct(1, request);

        assertThat(response).isNotNull();
        assertThat(sampleProduct.getProductName()).isEqualTo("MacBook Pro M3 Max");
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    @DisplayName("Delete Product: should remove product from repository")
    void testDeleteProduct_Success() {
        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        doNothing().when(productRepository).delete(sampleProduct);

        ApiResponse<String> response = productService.deleteProduct(1);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("deleted successfully");
        verify(productRepository, times(1)).delete(sampleProduct);
    }

    @Test
    @DisplayName("Add Item to Product: should save item and return ItemResponse")
    void testAddItemToProduct_Success() {
        ItemCreateRequest request = new ItemCreateRequest(25);
        Item item = new Item(sampleProduct, 25);
        item.setId(10);

        when(productRepository.findById(1)).thenReturn(Optional.of(sampleProduct));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponse response = productService.addItemToProduct(1, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10);
        assertThat(response.getQuantity()).isEqualTo(25);
        assertThat(response.getProductId()).isEqualTo(1);
        verify(itemRepository, times(1)).save(any(Item.class));
    }
}
