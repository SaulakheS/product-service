package com.zestindia.productservice.controller;

import com.zestindia.productservice.dto.request.ItemCreateRequest;
import com.zestindia.productservice.dto.request.ProductCreateRequest;
import com.zestindia.productservice.dto.request.ProductUpdateRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.ItemResponse;
import com.zestindia.productservice.dto.response.PagedResponse;
import com.zestindia.productservice.dto.response.ProductResponse;
import com.zestindia.productservice.service.ProductService;
import com.zestindia.productservice.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "Endpoints for managing products and their associated items")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new product", description = "Creates a product with automated auditing fields (createdBy, createdOn).")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        return new ResponseEntity<>(ApiResponse.success("Product created successfully!", response), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get paginated list of products", description = "Retrieves all products with support for pagination, sorting, and name filtering.")
    public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAllProducts(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir,
            @RequestParam(value = "search", required = false) String search
    ) {
        PagedResponse<ProductResponse> pagedResponse = productService.getAllProducts(page, size, sortBy, sortDir, search);
        return ResponseEntity.ok(ApiResponse.success("Products fetched successfully!", pagedResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Fetches a single product by its unique identifier along with its items.")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Integer id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product fetched successfully!", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update an existing product", description = "Updates product information and sets modifiedBy, modifiedOn auditing fields.")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully!", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete product by ID", description = "Deletes a product and cascades deletion to associated items. Requires ROLE_ADMIN.")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Integer id) {
        ApiResponse<String> response = productService.deleteProduct(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Get items of a product", description = "Retrieves all items associated with a given product ID.")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItemsByProductId(@PathVariable("id") Integer productId) {
        List<ItemResponse> items = productService.getItemsByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success("Items fetched successfully!", items));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add an item to a product", description = "Associates a new inventory item and quantity to a product.")
    public ResponseEntity<ApiResponse<ItemResponse>> addItemToProduct(
            @PathVariable("id") Integer productId,
            @Valid @RequestBody ItemCreateRequest request
    ) {
        ItemResponse response = productService.addItemToProduct(productId, request);
        return new ResponseEntity<>(ApiResponse.success("Item added successfully!", response), HttpStatus.CREATED);
    }
}
