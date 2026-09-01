package com.zestindia.productservice.service;

import com.zestindia.productservice.dto.request.ItemUpdateRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.ItemResponse;
import com.zestindia.productservice.entity.Item;
import com.zestindia.productservice.entity.Product;
import com.zestindia.productservice.event.ProductActivityEvent;
import com.zestindia.productservice.exception.ResourceNotFoundException;
import com.zestindia.productservice.repository.ItemRepository;
import com.zestindia.productservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Product sampleProduct;
    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("Smartphone");
        sampleProduct.setId(1);

        sampleItem = new Item(sampleProduct, 50);
        sampleItem.setId(10);
    }

    @Test
    @DisplayName("Get Item by ID: should return ItemResponse when found")
    void testGetItemById_Success() {
        when(itemRepository.findById(10)).thenReturn(Optional.of(sampleItem));

        ItemResponse response = itemService.getItemById(10);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10);
        assertThat(response.getQuantity()).isEqualTo(50);
        assertThat(response.getProductId()).isEqualTo(1);
        verify(itemRepository, times(1)).findById(10);
    }

    @Test
    @DisplayName("Get Item by ID: should throw ResourceNotFoundException when not found")
    void testGetItemById_NotFound() {
        when(itemRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Item not found with id: '99'");

        verify(itemRepository, times(1)).findById(99);
    }

    @Test
    @DisplayName("Update Item: should update quantity and publish activity event")
    void testUpdateItem_Success() {
        ItemUpdateRequest request = new ItemUpdateRequest(75);
        when(itemRepository.findById(10)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(Item.class))).thenReturn(sampleItem);

        ItemResponse response = itemService.updateItem(10, request);

        assertThat(response).isNotNull();
        assertThat(sampleItem.getQuantity()).isEqualTo(75);
        verify(itemRepository, times(1)).save(sampleItem);
        verify(eventPublisher, times(1)).publishEvent(any(ProductActivityEvent.class));
    }

    @Test
    @DisplayName("Delete Item: should delete item and publish activity event")
    void testDeleteItem_Success() {
        when(itemRepository.findById(10)).thenReturn(Optional.of(sampleItem));
        doNothing().when(itemRepository).delete(sampleItem);

        ApiResponse<String> response = itemService.deleteItem(10);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("deleted successfully");
        verify(itemRepository, times(1)).delete(sampleItem);
        verify(eventPublisher, times(1)).publishEvent(any(ProductActivityEvent.class));
    }
}
