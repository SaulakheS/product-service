package com.zestindia.productservice.controller;

import com.zestindia.productservice.dto.request.ItemUpdateRequest;
import com.zestindia.productservice.dto.response.ApiResponse;
import com.zestindia.productservice.dto.response.ItemResponse;
import com.zestindia.productservice.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/items")
@Tag(name = "Item Management", description = "Endpoints for managing individual inventory items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Fetches a single item by its ID.")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Integer id) {
        ItemResponse response = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success("Item fetched successfully!", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update item quantity", description = "Updates the quantity of an item.")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Integer id,
            @Valid @RequestBody ItemUpdateRequest request
    ) {
        ItemResponse response = itemService.updateItem(id, request);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully!", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete item by ID", description = "Deletes an item from inventory. Requires ROLE_ADMIN.")
    public ResponseEntity<ApiResponse<String>> deleteItem(@PathVariable Integer id) {
        ApiResponse<String> response = itemService.deleteItem(id);
        return ResponseEntity.ok(response);
    }
}
