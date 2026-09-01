package com.zestindia.productservice.service.impl;

import com.zestindia.productservice.dto.request.ItemUpdateRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.ItemResponse;
import com.zestindia.productservice.entity.Item;
import com.zestindia.productservice.event.ProductActivityEvent;
import com.zestindia.productservice.exception.ResourceNotFoundException;
import com.zestindia.productservice.repository.ItemRepository;
import com.zestindia.productservice.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ItemServiceImpl(ItemRepository itemRepository, ApplicationEventPublisher eventPublisher) {
        this.itemRepository = itemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Integer id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        return mapToItemResponse(item);
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Integer id, ItemUpdateRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        item.setQuantity(request.getQuantity());
        Item updatedItem = itemRepository.save(item);

        String currentUser = getCurrentUsername();
        if (item.getProduct() != null) {
            eventPublisher.publishEvent(new ProductActivityEvent(
                    ProductActivityEvent.ActivityType.ITEM_UPDATED,
                    item.getProduct().getId(),
                    item.getProduct().getProductName(),
                    currentUser
            ));
        }

        log.info("Item updated with ID: {}, new quantity: {}", updatedItem.getId(), updatedItem.getQuantity());
        return mapToItemResponse(updatedItem);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteItem(Integer id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", id));

        Integer productId = item.getProduct() != null ? item.getProduct().getId() : null;
        String productName = item.getProduct() != null ? item.getProduct().getProductName() : "Unknown";

        itemRepository.delete(item);

        String currentUser = getCurrentUsername();
        if (productId != null) {
            eventPublisher.publishEvent(new ProductActivityEvent(
                    ProductActivityEvent.ActivityType.ITEM_DELETED,
                    productId,
                    productName,
                    currentUser
            ));
        }

        log.info("Item deleted with ID: {}", id);
        return ApiResponse.success(String.format("Item with ID '%d' deleted successfully!", id));
    }

    private ItemResponse mapToItemResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getProduct() != null ? item.getProduct().getId() : null,
                item.getQuantity()
        );
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : "SYSTEM";
    }
}
