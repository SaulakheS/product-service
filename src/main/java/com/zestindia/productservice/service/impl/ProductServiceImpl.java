package com.zestindia.productservice.service.impl;

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
import com.zestindia.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProductServiceImpl(ProductRepository productRepository,
                              ItemRepository itemRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.itemRepository = itemRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = new Product(request.getProductName().trim());
        Product savedProduct = productRepository.save(product);

        String currentUser = getCurrentUsername();
        eventPublisher.publishEvent(new ProductActivityEvent(
                ProductActivityEvent.ActivityType.CREATED,
                savedProduct.getId(),
                savedProduct.getProductName(),
                currentUser
        ));

        log.info("Product created with ID: {} by user: {}", savedProduct.getId(), currentUser);
        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir, String search) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productsPage;

        if (StringUtils.hasText(search)) {
            productsPage = productRepository.findByProductNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            productsPage = productRepository.findAll(pageable);
        }

        List<ProductResponse> content = productsPage.getContent().stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                content,
                productsPage.getNumber(),
                productsPage.getSize(),
                productsPage.getTotalElements(),
                productsPage.getTotalPages(),
                productsPage.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Integer id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setProductName(request.getProductName().trim());
        Product updatedProduct = productRepository.save(product);

        String currentUser = getCurrentUsername();
        eventPublisher.publishEvent(new ProductActivityEvent(
                ProductActivityEvent.ActivityType.UPDATED,
                updatedProduct.getId(),
                updatedProduct.getProductName(),
                currentUser
        ));

        log.info("Product updated with ID: {} by user: {}", updatedProduct.getId(), currentUser);
        return mapToProductResponse(updatedProduct);
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        String productName = product.getProductName();
        productRepository.delete(product);

        String currentUser = getCurrentUsername();
        eventPublisher.publishEvent(new ProductActivityEvent(
                ProductActivityEvent.ActivityType.DELETED,
                id,
                productName,
                currentUser
        ));

        log.info("Product deleted with ID: {} by user: {}", id, currentUser);
        return ApiResponse.success(String.format("Product with ID '%d' deleted successfully!", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByProductId(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        List<Item> items = itemRepository.findByProductId(productId);
        return items.stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponse addItemToProduct(Integer productId, ItemCreateRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Item item = new Item(product, request.getQuantity());
        Item savedItem = itemRepository.save(item);

        String currentUser = getCurrentUsername();
        eventPublisher.publishEvent(new ProductActivityEvent(
                ProductActivityEvent.ActivityType.ITEM_ADDED,
                productId,
                product.getProductName(),
                currentUser
        ));

        log.info("Item added to Product ID: {}, Item ID: {}, Quantity: {}", productId, savedItem.getId(), savedItem.getQuantity());
        return mapToItemResponse(savedItem);
    }

    private ProductResponse mapToProductResponse(Product product) {
        List<ItemResponse> itemResponses = product.getItems() != null ?
                product.getItems().stream()
                        .map(this::mapToItemResponse)
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return new ProductResponse(
                product.getId(),
                product.getProductName(),
                product.getCreatedBy(),
                product.getCreatedOn(),
                product.getModifiedBy(),
                product.getModifiedOn(),
                itemResponses
        );
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
